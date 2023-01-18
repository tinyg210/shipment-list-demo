package dev.ancaghenade.shipmentlistdemo.changelogs;

import static java.lang.String.*;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangelogHelper {

  private static final Logger logger = LoggerFactory.getLogger(ChangelogHelper.class);

  public static void createTable(DynamoDB dynamoDB, String tableName, long readCapacityUnits,
      long writeCapacityUnits,
      String partitionKeyName, String partitionKeyType) {

    createTable(dynamoDB, tableName, readCapacityUnits, writeCapacityUnits,
        partitionKeyName, partitionKeyType, null, null);
  }

  public static void createTable(DynamoDB dynamoDB, String tableName, long readCapacityUnits,
      long writeCapacityUnits,
      String partitionKeyName, String partitionKeyType, String sortKeyName, String sortKeyType) {

    try {

      ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
      keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName)
          .withKeyType(KeyType.HASH)); // Partition key + type

      ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
      attributeDefinitions
          .add(new AttributeDefinition().withAttributeName(partitionKeyName)
              .withAttributeType(partitionKeyType));

      if (sortKeyName != null) {
        keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName)
            .withKeyType(KeyType.RANGE)); // Sort
        // key
        attributeDefinitions
            .add(new AttributeDefinition().withAttributeName(sortKeyName)
                .withAttributeType(sortKeyType));
      }

      CreateTableRequest request = new CreateTableRequest().withTableName(tableName)
          .withKeySchema(keySchema)
          .withProvisionedThroughput(
              new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                  .withWriteCapacityUnits(writeCapacityUnits));

      request.setAttributeDefinitions(attributeDefinitions);

      logger.info(format("Issuing CreateTable request for %s", tableName));
      Table table = dynamoDB.createTable(request);
      logger.info(format("Waiting for \"%s\" to be created...this may take a while...", tableName));

      table.waitForActive();

    } catch (Exception e) {
      logger.error(format("CreateTable request failed for %s",tableName));
      throw new RuntimeException(e.getMessage());
    }
  }


}
