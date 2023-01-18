package dev.ancaghenade.shipmentlistdemo.repository;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class S3StorageService {

  private final AmazonS3 s3;

  @Autowired
  public S3StorageService(AmazonS3 s3) {
    this.s3 = s3;
  }

  public void save(String path, String fileName,
      InputStream file, Optional<Map<String, String>> optionalMetadata) {
    ObjectMetadata fileMetadata = new ObjectMetadata();
    optionalMetadata.ifPresent(map -> {
      if (!map.isEmpty()) {
        map.forEach(fileMetadata::addUserMetadata);
      }
    });
    try {
      s3.putObject(path, fileName, file, fileMetadata);
    } catch (AmazonServiceException e) {
      throw new IllegalStateException("Could not save file to S3.", e);
    }

  }

  public byte[] download(String path, String key) {
    try {
      return IOUtils.toByteArray(s3.getObject(path, key).getObjectContent());
    } catch (AmazonServiceException e) {
      throw new IllegalStateException("Failed to download file.", e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
