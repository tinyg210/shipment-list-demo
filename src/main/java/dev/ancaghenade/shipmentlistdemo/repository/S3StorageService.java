package dev.ancaghenade.shipmentlistdemo.repository;

import dev.ancaghenade.shipmentlistdemo.buckets.BucketName;
import dev.ancaghenade.shipmentlistdemo.util.FileUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

  private final S3Client s3;
  private static final Logger LOGGER = LoggerFactory.getLogger(S3StorageService.class);

  @Autowired
  public S3StorageService(S3Client s3) {
    this.s3 = s3;
  }

  public void save(String path, String fileName,
      MultipartFile multipartFile)
      throws IOException {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(BucketName.SHIPMENT_PICTURE.getBucketName())
        .key(path + "/" + fileName)
        .contentType(multipartFile.getContentType())
        .contentLength(multipartFile.getSize())
        .build();

    s3.putObject(putObjectRequest,
        RequestBody.fromFile(FileUtil.convertMultipartFileToFile(multipartFile)));

  }

  public byte[] download(String key) throws IOException {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(BucketName.SHIPMENT_PICTURE.getBucketName())
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

}
