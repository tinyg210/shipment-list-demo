package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.net.URI;
import java.util.Objects;
import software.amazon.awssdk.services.sns.SnsClient;

public class SNSClientHelper {

  private static final String LOCALSTACK_HOSTNAME = System.getenv("LOCALSTACK_HOSTNAME");
  private static String snsTopicArn;

  public static SnsClient getSnsClient() {

    var clientBuilder = SnsClient.builder();

    if (Objects.nonNull(LOCALSTACK_HOSTNAME)) {
      snsTopicArn = String.format("arn:aws:sns:%s:000000000000:update_shipment_picture_topic",
          Location.REGION.getRegion());

      return clientBuilder
          .region(Location.REGION.getRegion())
          .endpointOverride(URI.create(String.format("http://%s:4566", LOCALSTACK_HOSTNAME)))
          .build();
    } else {
      snsTopicArn = String.format("arn:aws:sns:%s:%s:update_shipment_picture_topic",
          Location.REGION.getRegion(), "932043840972");
      return clientBuilder.build();
    }
  }

  public static String topicARN() {
    return snsTopicArn;
  }

}
