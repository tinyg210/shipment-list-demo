package dev.ancaghenade.shipmentlistdemo.repository;

import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@Repository
public class DynamoDBService {

  private final DynamoDbEnhancedClient dynamoDbClient;
  private final DynamoDbTable<Shipment> shipmentTable;

  @Autowired
  public DynamoDBService(DynamoDbEnhancedClient dynamoDbClient,
      DynamoDbTable<Shipment> shipmentTable) {
    this.dynamoDbClient = dynamoDbClient;
    this.shipmentTable = shipmentTable;
  }

  public Shipment upsert(Shipment shipment) {
    if (Objects.isNull(shipment.getShipmentId())) {
      shipmentTable.putItem(shipment);
    } else {
      shipmentTable.updateItem(shipment);
    }
    return shipment;
  }

  public Optional<Shipment> getShipment(String shipmentId) {
    return Optional.ofNullable(shipmentTable.getItem(Key.builder().partitionValue(shipmentId).build()));
  }

  public String delete(String shipmentId) {
    shipmentTable.deleteItem(Key.builder().partitionValue(shipmentId).build());
    return "Shipment has been deleted";
  }

  public List<Shipment> getAllShipments() {
    ScanEnhancedRequest request = ScanEnhancedRequest.builder().build();
    SdkIterable<Shipment> shipments = shipmentTable.scan(request).items();
    return shipments.stream().toList();
  }

}
