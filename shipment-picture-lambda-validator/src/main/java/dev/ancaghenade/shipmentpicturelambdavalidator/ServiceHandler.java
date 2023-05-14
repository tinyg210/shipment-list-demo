package dev.ancaghenade.shipmentpicturelambdavalidator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.jayway.jsonpath.JsonPath;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.http.entity.ContentType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

// Should be something more interesting

public class ServiceHandler implements RequestStreamHandler {

  private static final String BUCKET_NAME = "shipment-picture-bucket";

  private static PropertiesProvider properties = new PropertiesProvider();

  private boolean isValid = true;

  public ServiceHandler() throws IOException {
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream,
      Context context) throws IOException {

    var s3Client = acquireS3Client();
    var snsClient = acquireSnsClient();
    var objectKey = getObjectKey(inputStream, context);

    if (Objects.nonNull(objectKey)) {

      context.getLogger().log("Object key: " + objectKey);

      var getObjectRequest = GetObjectRequest.builder()
          .bucket(BUCKET_NAME)
          .key(objectKey)
          .build();

      var s3ObjectResponse = s3Client.getObject(
          getObjectRequest);

      context.getLogger().log("Object fetched");

      // Check if the image was already processed
      if (s3ObjectResponse.response().metadata().entrySet().stream().anyMatch(
          entry -> entry.getKey().equals("exclude-lambda") && entry.getValue().equals("true"))) {
        context.getLogger().log("Object already present");

        return;
      }

      // Check the file extension to determine the image format
      if (!List.of(ContentType.IMAGE_JPEG.getMimeType(),
              ContentType.IMAGE_PNG.getMimeType(),
              ContentType.IMAGE_BMP.getMimeType())
          .contains(s3ObjectResponse.response().contentType())) {
        isValid = false;
        context.getLogger().log("Object invalid");

      }

      // Get the object data as a byte array
      var objectData = s3Client.getObject(getObjectRequest).readAllBytes();

      if (!isValid) {
        var deleteRequest = DeleteObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(objectKey)
            .build();
        try {
          s3Client.deleteObject(deleteRequest);
          context.getLogger().log("Object deleted successfully!");
        } catch (S3Exception e) {
          context.getLogger().log(e.awsErrorDetails().errorMessage());
          System.exit(1);
        }

      } else {
        context.getLogger().log("PUT back");

        var extension = s3ObjectResponse.response().contentType();
        context.getLogger().log("GOT extension: "+ extension);

        var putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(objectKey)
            .metadata(Collections.singletonMap("exclude-lambda", "true"))
            .build();
        context.getLogger().log("Built request");

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(
            watermarkImage(objectData, extension.substring(extension.lastIndexOf("/") + 1))));
        context.getLogger().log("Watermark has been added.");

        context.getLogger().log("SNS send msg");

        var request = PublishRequest
            .builder()
            .message(objectKey)
            .topicArn("arn:aws:sns:eu-central-1:932043840972:update_shipment_picture_topic")
            .build();
        snsClient.publish(request);
        context.getLogger().log("Published to topic: " + request.topicArn());
      }
      // Close clients
      s3Client.close();
      snsClient.close();

    } else {
      context.getLogger().log("Object key is null");
    }
  }

  private byte[] watermarkImage(byte[] objectData, String extension)
      throws IOException {
    var originalImage = ImageIO.read(new ByteArrayInputStream(objectData));
    var watermarkedImage = new BufferedImage(originalImage.getWidth(),
        originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    var g2d = (Graphics2D) watermarkedImage.getGraphics();
    g2d.drawImage(originalImage, 0, 0, null);
    var font = new Font("Arial", Font.BOLD, 40);
    var color = Color.WHITE;

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

    g2d.setFont(font);
    g2d.setColor(color);
    g2d.drawString("Mine now!", 100, 100);

    var baos = new ByteArrayOutputStream();
    ImageIO.write(watermarkedImage, extension, baos);
    baos.close();
    return baos.toByteArray();

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
    try {
      return SNSClientHelper.getSnsClient();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

