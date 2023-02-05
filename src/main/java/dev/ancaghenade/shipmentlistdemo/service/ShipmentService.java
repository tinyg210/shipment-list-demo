package dev.ancaghenade.shipmentlistdemo.service;

import static java.lang.String.format;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import dev.ancaghenade.shipmentlistdemo.buckets.BucketName;
import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import dev.ancaghenade.shipmentlistdemo.repository.S3StorageService;
import dev.ancaghenade.shipmentlistdemo.repository.DynamoDBService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   return dynamoDBService.delete(shipmentId);
  }

  public Shipment saveShipment(Shipment shipment) {
    return dynamoDBService.upsert(shipment);
  }

  public void uploadShipmentImage(String shipmentId, MultipartFile file) {

    checkIfFileIsEmpty(file);

    Shipment shipment = getShipment(shipmentId);

    Map<String, String> metadata = getMetadata(file);

    String path = format("%s/%s", BucketName.SHIPMENT_PICTURE.getBucketName(),
        shipment.getShipmentId());

    String fileName = format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());
    try {
      s3StorageService.save(path, fileName, file.getInputStream(), Optional.of(metadata));
    } catch (IOException | AmazonS3Exception e) {
      throw new IllegalStateException(e);
    }
    shipment.setImageLink(fileName);
    dynamoDBService.upsert(shipment);
  }


  public byte[] downloadShipmentImage(String shipmentId) {
    Shipment shipment = dynamoDBService.getShipment(shipmentId).stream()
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("Shipment %s was not found.", shipmentId)));

    String path = format("%s/%s", BucketName.SHIPMENT_PICTURE.getBucketName(),
        shipment.getShipmentId());
    try {
      return Optional.ofNullable(shipment.getImageLink())
          .map(link -> s3StorageService.download(path, link))
          .orElse(Files.readAllBytes(new File("src/main/resources/placeholder.jpg").toPath()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private Map<String, String> getMetadata(MultipartFile file) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("Content-Type", file.getContentType());
    metadata.put("Content-Length", String.valueOf(file.getSize()));
    return metadata;
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


}
