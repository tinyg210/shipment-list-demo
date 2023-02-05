package dev.ancaghenade.shipmentlistdemo.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DynamoDBService {

  private final DynamoDBMapper dynamoDBMapper;

  @Autowired
  public DynamoDBService(DynamoDBMapper dynamoDBMapper) {
    this.dynamoDBMapper = dynamoDBMapper;
  }

  public Shipment upsert(Shipment shipment) {
    if (Objects.isNull(shipment.getShipmentId())) {
      dynamoDBMapper.save(shipment);
    }
    Shipment shp = dynamoDBMapper.load(Shipment.class, shipment.getShipmentId());
    if (Objects.isNull(shp)) {
      dynamoDBMapper.save(shipment);
    } else {
      dynamoDBMapper.save(shipment, new DynamoDBSaveExpression().withExpectedEntry("shipmentId",
          new ExpectedAttributeValue(new AttributeValue()
              .withS(shipment.getShipmentId()))));
    }
    return shipment;
  }

  public Optional<Shipment> getShipment(String shipmentId) {
    return Optional.ofNullable(dynamoDBMapper.load(Shipment.class, shipmentId));
  }

  public String delete(String shipmentId) {
    Shipment shipment = dynamoDBMapper.load(Shipment.class, shipmentId);
    dynamoDBMapper.delete(shipment);
    return "Shipment has been deleted";
  }

  public List<Shipment> getAllShipments() {
    return dynamoDBMapper.scan(Shipment.class, new DynamoDBScanExpression());
  }

}
