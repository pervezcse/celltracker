package com.rokin.celltracker.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@IdClass(ClientCirclePk.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCircle implements Serializable {

  public enum Role {
    MEMBER, OWNER
  }

  private static final long serialVersionUID = 1L;
  @Id
  @ManyToOne
  private Client client;
  @Id
  @ManyToOne
  private Circle circle;
  @Enumerated(EnumType.STRING)
  private Role role;
  private Boolean isInCircle;
  private Long jointimestamp;
}
