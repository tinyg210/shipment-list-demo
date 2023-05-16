package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.io.IOException;
import java.net.URI;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public class SNSClientHelper {

  private static final String ENVIRONMENT = System.getenv("ENVIRONMENT");
  private static PropertiesProvider properties = new PropertiesProvider();

  private static String snsTopicArn;

  public static SnsClient getSnsClient() throws IOException {

    var clientBuilder = SnsClient.builder();

    if (properties.getProperty("environment.dev").equals(ENVIRONMENT)) {
      snsTopicArn = properties.getProperty("sns.arn.dev");

      return clientBuilder
          .region(Region.of(properties.getProperty("aws.region")))
          .endpointOverride(URI.create(properties.getProperty("sns.endpoint")))
          .build();
    } else {
      snsTopicArn = properties.getProperty("sns.arn.prod");
      return clientBuilder.build();
    }
  }

  public static String topicARN() {
    return snsTopicArn;
  }

}
