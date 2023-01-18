package dev.ancaghenade.shipmentlistdemo.changelogs;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamobee.changeset.ChangeLog;
import com.github.dynamobee.changeset.ChangeSet;
import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import dev.ancaghenade.shipmentlistdemo.util.ResourceReader;
import java.io.IOException;

@ChangeLog
public class DatabaseChangelog {


  @ChangeSet(order = "001", id = "createShipmentTable", author = "anca")
  public void createShipmentTable(AmazonDynamoDB amazonDynamoDB) {
    DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

    ChangelogHelper.createTable(dynamoDB, "shipment", 10L,
        5L, "shipmentId", "S");
  }

  @ChangeSet(order = "002", id = "createShipmentData", author = "anca")
  public void createShipmentData(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper)
      throws IOException {

    // Some sample data to have at startup
    final ObjectMapper objectMapper = new ObjectMapper();
    Shipment[] shipmentList = objectMapper.readValue(
        ResourceReader.readFileToString("data/sample_shipments.json"), Shipment[].class);
    dynamoDBMapper.batchSave(shipmentList);
  }
}

