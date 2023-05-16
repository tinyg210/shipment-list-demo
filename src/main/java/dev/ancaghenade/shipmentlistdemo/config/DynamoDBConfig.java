package dev.ancaghenade.shipmentlistdemo.config;

import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDBConfig extends AWSClientConfig {

  @Value("${aws.dynamodb.endpoint}")
  private String awsDynamoDBEndPoint;

  @Bean
  public DynamoDbEnhancedClient dynamoDbClient() {
    DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(amazonAWSCredentialsProvider())
        .endpointOverride(URI.create(awsDynamoDBEndPoint))
        .build();

    // using the enhanced client for mapping classes to tables
    return DynamoDbEnhancedClient.builder()
        .dynamoDbClient(dynamoDbClient)
        .build();
  }
  @Bean
  public DynamoDbTable shipmentTable(DynamoDbEnhancedClient dynamoDbClient) {
    return dynamoDbClient.table("shipment", TableSchema.fromBean(Shipment.class));
  }


}
