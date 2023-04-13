package dev.ancaghenade.shipmentlistdemo.config;


import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AmazonS3Config extends AWSClientConfig {

  @Value("${aws.s3.endpoint}")
  private String awsS3EndPoint;

  @Bean
  public S3Client s3() {
    return S3Client.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(amazonAWSCredentialsProvider())
        .endpointOverride(URI.create(awsS3EndPoint))
        .build();
  }

}