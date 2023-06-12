package dev.ancaghenade.shipmentpicturelambdavalidator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.http.entity.ContentType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;


public class ServiceHandler implements RequestStreamHandler {

  private static final String BUCKET_NAME = System.getenv("BUCKET");
  public ServiceHandler() {
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream,
      Context context) throws IOException {
    var isValid = true;

    var s3Client = acquireS3Client();
    var snsClient = acquireSnsClient();
    var objectKey = getObjectKey(inputStream, context);

    if (Objects.isNull(objectKey)) {
      context.getLogger().log("Object key is null");
      return;
    }

    context.getLogger().log("Object key: " + objectKey);

    var getObjectRequest = GetObjectRequest.builder()
        .bucket(BUCKET_NAME)
        .key(objectKey)
        .build();

    ResponseInputStream<GetObjectResponse> s3ObjectResponse;
    try {
      s3ObjectResponse = s3Client.getObject(
          getObjectRequest);
    } catch (Exception e) {
      e.printStackTrace();
      context.getLogger().log(e.getMessage());
      return;
    }
    context.getLogger().log("Object fetched");

    // Check if the image was already processed
    if (s3ObjectResponse.response().metadata().entrySet().stream().anyMatch(
        entry -> entry.getKey().equals("exclude-lambda") && entry.getValue().equals("true"))) {
      context.getLogger().log("Object already present.");
      return;
    }

    // Check the file extension to determine the image format
    if (!List.of(ContentType.IMAGE_JPEG.getMimeType(),
            ContentType.IMAGE_PNG.getMimeType(),
            ContentType.IMAGE_BMP.getMimeType())
        .contains(s3ObjectResponse.response().contentType())) {
      isValid = false;
      context.getLogger().log("Object invalid due to wrong format.");

    }

    // Get the object data as a byte array
    var objectData = s3Client.getObject(getObjectRequest).readAllBytes();

    if (!isValid) {
      try {
        File imageFile = new File("placeholder.jpg");
        BufferedImage image = ImageIO.read(imageFile);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        objectKey = TextParser.replaceSufix(objectKey, "placeholder.jpg");

        System.out.println("NEW IMAGE LINK: " + objectKey);

        var putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(objectKey)
            .metadata(Collections.singletonMap("exclude-lambda", "true"))
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

        baos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      var extension = s3ObjectResponse.response().contentType();

      var putObjectRequest = PutObjectRequest.builder()
          .bucket(BUCKET_NAME)
          .key(objectKey)
          .metadata(Collections.singletonMap("exclude-lambda", "true"))
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(
          Watermark.watermarkImage(objectData,
              extension.substring(extension.lastIndexOf("/") + 1))));
      context.getLogger().log("Watermark has been added.");
    }
    var request = PublishRequest
        .builder()
        .message(objectKey)
        .topicArn(SNSClientHelper.topicARN())
        .build();
    snsClient.publish(request);
    context.getLogger().log("Published to topic: " + request.topicArn());

    // Close clients
    s3Client.close();
    snsClient.close();

  }

  private String getObjectKey(InputStream inputStream, Context context) {
    try {
      List<String> keys = JsonPath.read(inputStream, "$.Records[*].s3.object.key");
      if (keys.iterator().hasNext()) {
        return keys.iterator().next();
      }
    } catch (IOException ioe) {
      context.getLogger().log("caught IOException reading input stream");
    }
    return null;
  }

  private S3Client acquireS3Client() {
    try {
      return S3ClientHelper.getS3Client();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private SnsClient acquireSnsClient() {
    return SNSClientHelper.getSnsClient();
  }
}

