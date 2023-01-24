package dev.ancaghenade.shipmentlistdemo.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {

  @Value("${aws.credentials.access-key}")
  private String awsAccessKey;

  @Value("${aws.credentials.secret-key}")
  private String awsSecretKey;

  @Value("${aws.region}")
  private String awsRegion;

  @Value("${aws.s3.endpoint}")
  private String awsS3EndPoint;

  @Bean
  public AmazonS3 s3() {
    AWSCredentials awsCredentials = new BasicAWSCredentials(
        awsAccessKey,
        awsSecretKey
    );
    AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(new EndpointConfiguration(awsS3EndPoint,
            awsRegion));

    return amazonS3ClientBuilder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
        .build();
  }

}