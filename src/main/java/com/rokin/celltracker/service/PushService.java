package com.rokin.celltracker.service;

import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.Message;
import com.rokin.celltracker.domain.Message.MessageType;

import de.bytefish.fcmjava.http.client.IFcmClient;
import de.bytefish.fcmjava.model.options.FcmMessageOptions;
import de.bytefish.fcmjava.requests.data.DataMulticastMessage;
import de.bytefish.fcmjava.requests.data.DataUnicastMessage;
import de.bytefish.fcmjava.requests.notification.NotificationPayload;
import de.bytefish.fcmjava.responses.FcmMessageResponse;
import de.bytefish.fcmjava.responses.FcmMessageResultItem;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushService {

  @Resource
  IFcmClient fcmClient;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Send Push Message.
   * 
   * @param msg
   *          push message
   * @param registratiodIds
   *          reg ids of cleitn devices
   */
  public CompletableFuture<Message> sendMessage(Message msg, List<String> registratiodIds) {
    return CompletableFuture.supplyAsync(() -> {
      multicastMessage("Cell Tracker notification",
          "Cell Tracker notification: " + msg.getMeassgeType(), "CellTracker", msg.getMeassgeType(),
          msg.getMessage(), msg.getFromClientId(), msg.getDeviceInfo(), registratiodIds);
      return msg;
    });
  }

  private void multicastMessage(String title, String body, String tag, MessageType meassgeType,
      String message, Long msgSender, ClientDeviceInfo deviceInfo, List<String> registratiodIds) {
    if (registratiodIds != null && !registratiodIds.isEmpty()) {
      FcmMessageOptions options = FcmMessageOptions.builder().setTimeToLive(Duration.ofHours(1))
          .build();
      NotificationPayload payload = NotificationPayload.builder().setBody(body).setTitle(title)
          .setTag(tag).build();
      Map<String, Object> data = new HashMap<>();
      data.put("messageType", meassgeType);
      data.put("message", message);
      data.put("fromClientId", msgSender);
      data.put("deviceInfo", deviceInfo);
      FcmMessageResponse response = null;
      if (registratiodIds.size() > 1) {
        DataMulticastMessage dmm = new DataMulticastMessage(options, registratiodIds, data,
            payload);
        response = this.fcmClient.send(dmm);
      } else {
        DataUnicastMessage dum = new DataUnicastMessage(options, registratiodIds.get(0), data,
            payload);
        response = this.fcmClient.send(dum);
      }
      for (FcmMessageResultItem result : response.getResults()) {
        if (result.getErrorCode() != null) {
          log.error("Sending to {} failed. Error Code {}\n", result.getCanonicalRegistrationId(),
              result.getErrorCode());
        }
      }
    }
  }
}
