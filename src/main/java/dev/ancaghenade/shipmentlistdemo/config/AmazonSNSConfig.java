package dev.ancaghenade.shipmentlistdemo.config;


import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AmazonSNSConfig extends AWSClientConfig {

  @Value("${aws.sns.endpoint}")
  private String awsSnsEndPoint;

  @Value("${aws.sns.topic}")
  private String awsSnsTopic;

  @Value("${aws.sns.subscriberEndpoint}")
  private String subscriberEndpoint;
  @Value("${aws.account}")
  private String awsAccountNumber;

  @Bean
  public SnsClient sns() {
    SnsClient snsClient = SnsClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(amazonAWSCredentialsProvider())
        .endpointOverride(URI.create(awsSnsEndPoint))
        .build();

//    SubscribeRequest subscribeRequest = SubscribeRequest.builder()
//        .topicArn(new StringJoiner(":")
//            .add("arn")
//            .add("aws")
//            .add("sns")
//            .add(awsRegion)
//            .add(awsAccountNumber)
//            .add(awsSnsTopic).toString())
//        .protocol("https")
//        .endpoint(subscriberEndpoint + "/sns/notifications")
//        .build();
//
//    snsClient.subscribe(subscribeRequest);
    return snsClient;
  }

}
