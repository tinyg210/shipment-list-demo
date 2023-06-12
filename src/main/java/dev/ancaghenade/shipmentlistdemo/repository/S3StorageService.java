package dev.ancaghenade.shipmentlistdemo.repository;

import dev.ancaghenade.shipmentlistdemo.buckets.BucketName;
import dev.ancaghenade.shipmentlistdemo.util.FileUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3StorageService {

  private final S3Client s3;
  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageService.class);

  private final BucketName bucketName;
  @Autowired
  public S3StorageService(S3Client s3, BucketName bucketName) {
    this.s3 = s3;
    this.bucketName = bucketName;
  }

  public void save(String path, String fileName,
      MultipartFile multipartFile)
      throws IOException {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName.getShipmentPictureBucket())
        .key(path + "/" + fileName)
        .contentType(multipartFile.getContentType())
        .contentLength(multipartFile.getSize())
        .build();

    s3.putObject(putObjectRequest,
        RequestBody.fromFile(FileUtil.convertMultipartFileToFile(multipartFile)));

  }

  public byte[] download(String key) throws IOException {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName.getShipmentPictureBucket())
        .key(key)
        .build();
    byte[] object = new byte[0];
    try {
      object = s3.getObject(getObjectRequest).readAllBytes();
    } catch (NoSuchKeyException noSuchKeyException) {
      LOGGER.warn(String.format("Could not find object: %s", noSuchKeyException.getMessage()));
    }
    return object;
  }

  public void delete(String folderPrefix) {
    List<ObjectIdentifier> keysToDelete = new ArrayList<>();
    s3.listObjectsV2Paginator(
            builder -> builder.bucket(bucketName.getShipmentPictureBucket())
                .prefix(folderPrefix + "/"))
        .contents().stream()
        .map(S3Object::key)
        .forEach(key -> keysToDelete.add(ObjectIdentifier.builder().key(key).build()));

    DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
        .bucket(bucketName.getShipmentPictureBucket())
        .delete(builder -> builder.objects(keysToDelete).build())
        .build();

    try {
      DeleteObjectsResponse response = s3.deleteObjects(deleteRequest);
      List<S3Error> errors = response.errors();
      if (!errors.isEmpty()) {
        LOGGER.error("Errors occurred while deleting objects:");
        errors.forEach(error -> System.out.println("Object: " + error.key() +
            ", Error Code: " + error.code() +
            ", Error Message: " + error.message()));
      } else {
        LOGGER.info("Objects deleted successfully.");
      }
    } catch (SdkException e) {
      LOGGER.error("Error occurred during object deletion: " + e.getMessage());
    }
  }

}
