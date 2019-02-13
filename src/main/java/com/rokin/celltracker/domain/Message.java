package com.rokin.celltracker.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

  public enum MessageType {
    HELPALERT, LOCATIONQUERY, REACHEDHOME, REACHEDOFFICE, IM, CUSTOM
  }
  
  public enum MessageScope {
    BROADCAST, CIRCLE, UNICAST
  }
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private Long fromClientId;
  private String toCircleId;
  private Long toClientId;
  @NotNull
  private MessageType meassgeType;
  @NotNull
  private MessageScope messageScope;
  private String message;
  @OneToOne
  @NotNull
  private ClientDeviceInfo deviceInfo;
  private Boolean isSent;
}
