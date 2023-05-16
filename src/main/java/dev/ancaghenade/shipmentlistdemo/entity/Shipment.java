package dev.ancaghenade.shipmentlistdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Shipment {
  private String shipmentId;
  private Participant recipient;
  private Participant sender;
  private Double weight;
  private String imageLink;


  @DynamoDbPartitionKey
  public String getShipmentId() {
    return shipmentId;
  }

  public void setShipmentId(String shipmentId) {
    this.shipmentId = shipmentId;
  }

  @NonNull
  @DynamoDbAttribute("recipient")
  public Participant getRecipient() {
    return recipient;
  }

  public void setRecipient(@NonNull Participant recipient) {
    this.recipient = recipient;
  }

  @NonNull
  @DynamoDbAttribute("sender")
  public Participant getSender() {
    return sender;
  }

  public void setSender(@NonNull Participant sender) {
    this.sender = sender;
  }

  @DynamoDbAttribute("weight")
  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }

  @DynamoDbAttribute("imageLink")

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }
}