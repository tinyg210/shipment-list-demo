package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.io.IOException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public class SNSClientHelper {

  private static final String ENVIRONMENT = System.getenv("ENVIRONMENT");
  private static PropertiesProvider properties = new PropertiesProvider();

  public static SnsClient getSnsClient() throws IOException {

    if (properties.getProperty("environment.dev").equals(ENVIRONMENT)) {

      return SnsClient.builder()
          .region(Region.of("eu-central-1"))
          .credentialsProvider(StaticCredentialsProvider.create(
              AwsBasicCredentials.create(properties.getProperty("credentials.access-key"),
                  properties.getProperty("credentials.secret-key"))))
          .endpointOverride(java.net.URI.create("http://localstack:4566/s"))
          .build();
    } else {
      System.out.println("AWS SNS client is used");

      return SnsClient.builder().build();
    }
  }

}
