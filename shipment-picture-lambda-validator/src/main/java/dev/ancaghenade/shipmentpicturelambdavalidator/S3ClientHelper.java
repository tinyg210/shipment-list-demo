package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.io.IOException;
import java.net.URI;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientHelper {

  private static final String ENVIRONMENT = System.getenv("ENVIRONMENT");
  private static PropertiesProvider properties = new PropertiesProvider();

  public static S3Client getS3Client() throws IOException {

    var clientBuilder = S3Client.builder();
    if (properties.getProperty("environment.dev").equals(ENVIRONMENT)) {

      return clientBuilder
          .region(Region.of(properties.getProperty("aws.region")))
          .endpointOverride(URI.create(properties.getProperty("s3.endpoint")))
          .forcePathStyle(true)
          .build();
    } else {
      return clientBuilder.build();
    }
  }

}
