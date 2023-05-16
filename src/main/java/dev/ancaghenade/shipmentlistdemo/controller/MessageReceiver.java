package dev.ancaghenade.shipmentlistdemo.controller;

import dev.ancaghenade.shipmentlistdemo.service.ShipmentService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RestController
public class MessageReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  private final ShipmentService shipmentService;

  @Autowired
  public MessageReceiver(ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  @SqsListener(value = "update_shipment_picture_queue")
  public void loadMessagesFromQueue(String notification) {
    LOGGER.info("Message from queue %s" + notification);

    JSONObject obj = new JSONObject(notification);
    String message = obj.getString("Message");
    String shipmentId = message.split("/")[0];

    shipmentService.updateImageLink(shipmentId, message);

    for (var sseEmitter : emitters) {
      try {
        sseEmitter.send(
            shipmentId);
        sleep(sseEmitter);
      } catch (IOException e) {
        sseEmitter.completeWithError(e);
      }
      sseEmitter.complete();
      LOGGER.info("SSE emitter complete.");
    }
  }

  @GetMapping(value = "/push-endpoint")
  @CrossOrigin(origins = "http://localhost:3000")
  public SseEmitter pushData() {

    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    emitter.onCompletion(() -> {
      synchronized (emitters) {
        emitters.remove(emitter);
        LOGGER.info("SseEmitter is completed");
      }
    });

    emitter.onTimeout(() -> {
      synchronized (emitters) {
        emitters.remove(emitter);
      }
      emitter.complete();
      LOGGER.info("SseEmitter is timed out");
    });

    emitter.onError(e -> {
      synchronized (emitters) {
        emitters.remove(emitter);
      }
    });

    synchronized (emitters) {
      emitters.add(emitter);
    }

    return emitter;
  }

  private void sleep(SseEmitter sseEmitter) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
      sseEmitter.completeWithError(e);
    }
  }
}
