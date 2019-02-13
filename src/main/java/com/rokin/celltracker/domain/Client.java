package com.rokin.celltracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdentifiableClient
public class Client {
  
  public enum Role {
    CLIENT
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  @Column(unique = true)
  @Email
  private String email;
  @Column(unique = true)
  private String cellNo;
  @NotNull
  @Size(min = 4)
  @JsonIgnore
  private String password;
  private String deviceId;
  private String pushNotificationId;
  @OneToOne
  private ClientDeviceInfo latestDeviceInfo;
  @ElementCollection(fetch = FetchType.EAGER)
  private List<Role> roles;
  @Column(name = "enabled", nullable = false)
  private boolean enabled;
  private String imageFileName;
  private Long timestamp;

  /**
   * set latest device info.
   * @param latestDeviceInfo latest deivce info
   */
  public void setLatestDeviceInfo(ClientDeviceInfo latestDeviceInfo) {
    if (latestDeviceInfo != null) {
      latestDeviceInfo.setClient(this);
      this.latestDeviceInfo = latestDeviceInfo;
    }
  }

}
