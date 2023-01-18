package dev.ancaghenade.shipmentlistdemo.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.github.dynamobee.Dynamobee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfiguration {

  @Value("${aws.access.key}")
  private String awsAccessKey;

  @Value("${aws.access.secret-key}")
  private String awsSecretKey;

  @Value("${aws.dynamodb.endpoint}")
  private String awsDynamoDBEndPoint;

  @Value("${aws.region}")
  private String awsRegion;

  @Bean
  public DynamoDBMapper dynamoDBMapper() {
    return new DynamoDBMapper(buildAmazonDynamoDB());
  }

  @Bean
  public AWSCredentials amazonAWSCredentials() {
    return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
  }

  @Bean
  AmazonDynamoDB buildAmazonDynamoDB() {
    return AmazonDynamoDBClientBuilder
        .standard()
        .withEndpointConfiguration(
            new EndpointConfiguration(
                awsDynamoDBEndPoint,
                awsRegion
            )
        )
        .withCredentials(
            (awsAccessKey == null && awsSecretKey == null) ? null : amazonAWSCredentialsProvider())
        .build();
  }

  @Bean
  public Dynamobee dynamobee(AmazonDynamoDB dynamoDB) {
    Dynamobee runner = new Dynamobee(dynamoDB);
    runner.setChangeLogsScanPackage(
        "dev.ancaghenade.shipmentlistdemo.changelogs"); // the package to be scanned for changesets

    return runner;
  }

  public AWSCredentialsProvider amazonAWSCredentialsProvider() {
    return new AWSStaticCredentialsProvider(amazonAWSCredentials());
  }
}
