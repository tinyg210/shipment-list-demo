package dev.ancaghenade.shipmentlistdemo.service;

import static java.lang.String.format;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import dev.ancaghenade.shipmentlistdemo.buckets.BucketName;
import dev.ancaghenade.shipmentlistdemo.entity.Shipment;
import dev.ancaghenade.shipmentlistdemo.repository.S3StorageService;
import dev.ancaghenade.shipmentlistdemo.repository.ShipmentRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ShipmentService {


  private final ShipmentRepository shipmentRepository;
  private final S3StorageService s3StorageService;


  @Autowired
  public ShipmentService(ShipmentRepository shipmentRepository, S3StorageService s3StorageService) {
    this.shipmentRepository = shipmentRepository;
    this.s3StorageService = s3StorageService;
  }

  public List<Shipment> getAllShipments() {
    return shipmentRepository.getAllShipments();
  }

  public Shipment saveShipment(Shipment shipment) {
    return shipmentRepository.upsert(shipment);
  }

  public void uploadShipmentImage(String shipmentId, MultipartFile file) {

    checkIfFileIsEmpty(file);

    checkIfFileIsImage(file);

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
    shipmentRepository.upsert(shipment);
  }


  public byte[] downloadShipmentImage(String shipmentId) {
    Shipment shipment = shipmentRepository.getShipment(shipmentId).stream()
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("Shipment %s was not found.", shipmentId)));

    String path = format("%s/%s", BucketName.SHIPMENT_PICTURE.getBucketName(),
        shipment.getShipmentId());
    return Optional.ofNullable(shipment.getImageLink())
        .map(link -> s3StorageService.download(path, link))
        .orElse(new byte[0]);
  }


  private Map<String, String> getMetadata(MultipartFile file) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("Content-Type", file.getContentType());
    metadata.put("Content-Length", String.valueOf(file.getSize()));
    return metadata;
  }

  private Shipment getShipment(String shipmentId) {
    return shipmentRepository.getShipment(shipmentId).stream()
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("Shipment %s was not found.", shipmentId)));
  }

  private void checkIfFileIsImage(MultipartFile file) {
    if (!List.of(ContentType.IMAGE_JPEG.getMimeType(),
            ContentType.IMAGE_PNG.getMimeType(),
            ContentType.IMAGE_BMP.getMimeType(),
            ContentType.IMAGE_GIF.getMimeType())
        .contains(file.getContentType())) {
      throw new IllegalStateException(
          "File format not accepted.");
    }
  }

  private void checkIfFileIsEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalStateException(
          "Cannot save empty file to S3. File size: [" + file.getSize() + "]");
    }
  }


}
