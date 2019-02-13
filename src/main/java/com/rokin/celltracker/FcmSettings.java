package com.rokin.celltracker;

import de.bytefish.fcmjava.http.options.IFcmClientSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fcm")
@Component
@Data
public class FcmSettings implements IFcmClientSettings {
  private String apiKey;
  private String fcmUrl;
}
