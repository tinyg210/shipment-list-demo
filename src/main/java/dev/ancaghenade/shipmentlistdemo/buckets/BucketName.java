package dev.ancaghenade.shipmentlistdemo.buckets;

import lombok.Getter;

@Getter
public enum BucketName {

  SHIPMENT_PICTURE("shipment-list-demo-bucket");

  private final String bucketName;

  BucketName(String bucketName) {
    this.bucketName = bucketName;
  }
}
