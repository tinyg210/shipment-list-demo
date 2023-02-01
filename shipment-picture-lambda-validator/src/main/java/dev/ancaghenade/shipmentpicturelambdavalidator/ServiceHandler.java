package dev.ancaghenade.shipmentpicturelambdavalidator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.http.entity.ContentType;

// Should be something more interesting

public class ServiceHandler implements RequestHandler<S3Event, Void> {

  public Void handleRequest(S3Event event, Context context) {

    AmazonS3 s3Client = null;
    try {
      s3Client = S3ClientHelper.getS3Client();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    LambdaLogger logger = context.getLogger();
    boolean isValid = true;

    // check if record is there
    if (event.getRecords().isEmpty()) {
      logger.log("No records received.");
      return null;
    }

    for (S3EventNotificationRecord record : event.getRecords()) {
      String bucketName = record.getS3().getBucket().getName();
      String objectKey = record.getS3().getObject().getKey();

      S3Object s3Object = s3Client.getObject(bucketName, objectKey);

      if (!List.of(ContentType.IMAGE_JPEG.getMimeType(),
              ContentType.IMAGE_PNG.getMimeType(),
              ContentType.IMAGE_BMP.getMimeType())
          .contains(s3Object.getObjectMetadata().getUserMetadata().get("content-type"))) {

        isValid = false;
        logger.log(
            "File format not accepted. This will be replaced with a standard placeholder.");
      }
      if (isValid) {
        byte[] magicNumbers = new byte[4];
        InputStream objectData = s3Object.getObjectContent();
        try {
          objectData.read(magicNumbers, 0, 4);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (Arrays.equals(magicNumbers, new byte[]{(byte) 0x7f, 'E', 'L', 'F'})) {
          logger.log("The object is an ELF executable file.");
          isValid = false;

        } else if (Arrays.equals(magicNumbers, new byte[]{'M', 'Z'})) {
          logger.log("The object is a Windows executable file.");
          isValid = false;

        }
      }
      if (!isValid) {
        s3Client.deleteObject(bucketName, objectKey);

        InputStream is = ServiceHandler.class.getResourceAsStream("/resources/placeholder.jpg");
        if (is == null) {
          is = ServiceHandler.class.getClassLoader().getResourceAsStream("placeholder.jpg");
        }

        s3Client.putObject(new PutObjectRequest(bucketName, objectKey, is, new ObjectMetadata()));

      } else {
        logger.log(
            "Found image with content type: " + s3Object.getObjectMetadata().getUserMetadata()
                .get("content-type") + " that is correct.");
      }

    }
    return null;
  }
}
