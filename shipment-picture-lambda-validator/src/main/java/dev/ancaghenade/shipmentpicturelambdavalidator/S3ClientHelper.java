package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientHelper {

  private static final String LOCALSTACK_HOSTNAME = System.getenv("LOCALSTACK_HOSTNAME");

  public static S3Client getS3Client() throws IOException {

    var clientBuilder = S3Client.builder();
    if (Objects.nonNull(LOCALSTACK_HOSTNAME)) {
      return clientBuilder
          .region(Location.REGION.getRegion())
          .endpointOverride(URI.create(String.format("http://%s:4566", LOCALSTACK_HOSTNAME)))
          .forcePathStyle(true)
          .build();
    } else {
      return clientBuilder.build();
    }
  }

}
