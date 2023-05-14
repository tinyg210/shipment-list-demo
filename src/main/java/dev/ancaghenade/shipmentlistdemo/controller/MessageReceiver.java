package dev.ancaghenade.shipmentlistdemo.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class MessageReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();


  @PostMapping("/sns/notifications")
  public void handleNotification(@RequestBody String notification) {
    System.out.println(notification);
    JSONObject obj = new JSONObject(notification);
    String message = obj.getString("Message");
    String shipmentId = message.split("/")[0];
    for (SseEmitter sseEmitter : emitters) {
      try {
        sseEmitter.send(
            shipmentId);
        sleep(1, sseEmitter);
      } catch (IOException e) {
        LOGGER.info("Caught error: ");
        e.printStackTrace();
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

    emitter.onError((e) -> {
      synchronized (emitters) {
        emitters.remove(emitter);
        LOGGER.info("SseEmitter got an error: " + e.getMessage());
      }
    });

    synchronized (emitters) {
      emitters.add(emitter);
    }

    return emitter;
  }

  private void sleep(int seconds, SseEmitter sseEmitter) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
      sseEmitter.completeWithError(e);
    }
  }
}
