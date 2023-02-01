package dev.ancaghenade.shipmentpicturelambdavalidator;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.IOException;

public class S3ClientHelper {

  private static final String ENVIRONMENT = System.getenv("ENVIRONMENT");

  public static AmazonS3 getS3Client() throws IOException {
    PropertiesProvider properties = new PropertiesProvider();

    if (properties.values().getProperty("environment.dev").equals(ENVIRONMENT)) {
      AWSCredentials awsCredentials = new BasicAWSCredentials(
          "",
          ""
      );
      AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder
          .standard()
          .withEndpointConfiguration(
              new EndpointConfiguration(properties.values().getProperty("s3.url"),
                  properties.values().getProperty("s3.region")));

      return amazonS3ClientBuilder.withCredentials(
              new AWSStaticCredentialsProvider(awsCredentials))
          .build();
    } else {
      return AmazonS3ClientBuilder.defaultClient();
    }
  }
}
