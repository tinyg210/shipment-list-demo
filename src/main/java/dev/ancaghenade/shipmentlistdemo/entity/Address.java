package dev.ancaghenade.shipmentlistdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
public class Address {

  private String postalCode;
  private String street;
  private String number;
  private String city;
  private String additionalInfo;

  @DynamoDbAttribute("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @DynamoDbAttribute("street")

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  @DynamoDbAttribute("number")

  public String getNumber() {
    return number;
  }


  public void setNumber(String number) {
    this.number = number;
  }

  @DynamoDbAttribute("city")

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  @DynamoDbAttribute("additionalInfo")

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }
}