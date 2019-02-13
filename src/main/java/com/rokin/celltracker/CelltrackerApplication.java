package com.rokin.celltracker;

import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.http.client.IFcmClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CelltrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CelltrackerApplication.class, args);
  }

  @Bean
  public IFcmClient fcmClient(FcmSettings settings) {
    return new FcmClient(settings);
  }
}
