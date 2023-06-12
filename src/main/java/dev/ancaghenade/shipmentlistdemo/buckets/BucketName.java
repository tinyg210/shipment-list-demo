package dev.ancaghenade.shipmentlistdemo.buckets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@PropertySource(value = "classpath:buckets.properties")
public class BucketName {

  @Value("${shipment-picture-bucket}")
  private String shipmentPictureBucket;
  @Value("${shipment-picture-bucket-validator}")
  private String shipmentPictureValidatorBucket;

  public String getShipmentPictureBucket() {
    return shipmentPictureBucket;
  }

  public String getShipmentPictureValidatorBucket() {
    return shipmentPictureValidatorBucket;
  }
}
