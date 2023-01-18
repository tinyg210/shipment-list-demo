package dev.ancaghenade.shipmentlistdemo.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@DynamoDBDocument
public class Address {

  @DynamoDBAttribute
  private String postalCode;

  @DynamoDBAttribute
  private String street;

  @DynamoDBAttribute
  private String number;

  @DynamoDBAttribute
  private String city;

  @DynamoDBAttribute
  private String additionalInfo;
}