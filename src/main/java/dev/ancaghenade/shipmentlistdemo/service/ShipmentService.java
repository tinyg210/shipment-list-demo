package dev.ancaghenade.shipmentlistdemo.service;

import static java.lang.String.format;

import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import dev.ancaghenade.shipmentlistdemo.repository.DynamoDBService;
import dev.ancaghenade.shipmentlistdemo.repository.S3StorageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ShipmentService {


  private final DynamoDBService dynamoDBService;
  private final S3StorageService s3StorageService;


  @Autowired
  public ShipmentService(DynamoDBService dynamoDBService, S3StorageService s3StorageService) {
    this.dynamoDBService = dynamoDBService;
    this.s3StorageService = s3StorageService;
  }

  public List<Shipment> getAllShipments() {
    return dynamoDBService.getAllShipments();
  }

  public String deleteShipment(String shipmentId) {
    s3StorageService.delete(shipmentId);
    return dynamoDBService.delete(shipmentId);
  }

  public Shipment saveShipment(Shipment shipment) {
    return dynamoDBService.upsert(shipment);
  }

  public void removeImageLink(String shipmentId) {
    dynamoDBService.removeImageLink(shipmentId);
  }

  public void uploadShipmentImage(String shipmentId, MultipartFile file) {

    checkIfFileIsEmpty(file);

    Shipment shipment = getShipment(shipmentId);

    String path = shipment.getShipmentId();

    String fileName = format("%s-%s", UUID.randomUUID(), file.getOriginalFilename());
    try {
      s3StorageService.save(path, fileName, file);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    shipment.setImageLink(format("%s/%s", path, fileName));
    dynamoDBService.upsert(shipment);
  }


  public byte[] downloadShipmentImage(String shipmentId) throws IllegalStateException {
    Shipment shipment = dynamoDBService.getShipment(shipmentId).stream()
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("Shipment %s was not found.", shipmentId)));

    try {
      return Optional.ofNullable(shipment.getImageLink())
          .map(link -> {
            try {
              return s3StorageService.download(link);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          })
          .orElse(Files.readAllBytes(new File("src/main/resources/placeholder.jpg").toPath()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private Shipment getShipment(String shipmentId) {
    return dynamoDBService.getShipment(shipmentId).stream()
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("Shipment %s was not found.", shipmentId)));
  }

  private void checkIfFileIsEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalStateException(
          "Cannot save empty file to S3. File size: [" + file.getSize() + "]");
    }
  }


  public void updateImageLink(String shipmentId, String imageLink) {
    dynamoDBService.updateImageLink(shipmentId, imageLink);
  }
}
