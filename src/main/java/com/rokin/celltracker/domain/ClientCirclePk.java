package com.rokin.celltracker.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCirclePk implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private Long client;
  private String circle;
}
