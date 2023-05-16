package dev.ancaghenade.shipmentlistdemo.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AmazonSQSConfig extends AWSClientConfig {

  @Value("${aws.sqs.endpoint}")
  private String awsSqsEndPoint;

  @Bean
  public SqsAsyncClient sqsClient() {
    return SqsAsyncClient.builder()
        .endpointOverride(URI.create(awsSqsEndPoint))
        .credentialsProvider(amazonAWSCredentialsProvider())
        .region(Region.of(awsRegion))
        .build();
  }

}
