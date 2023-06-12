package dev.ancaghenade.shipmentpicturelambdavalidator;

import lombok.Getter;
import software.amazon.awssdk.regions.Region;

@Getter
public enum Location {


  REGION(Region.US_EAST_1);

  private final Region region;
  Location(Region region) {
    this.region = region;
  }
}
