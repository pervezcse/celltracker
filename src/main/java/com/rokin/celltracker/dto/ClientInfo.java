package com.rokin.celltracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rokin.celltracker.domain.Client.Role;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientInfo {

  @Email
  private String email;
  private String cellNo;
  @NotNull
  @Size(min = 4)
  @JsonIgnore
  private String password;
  private String deviceId;
  private String pushNotificationId;
  private ClientDeviceInfo latestDeviceInfo;
  private List<Role> roles;
  private boolean enabled;
}
