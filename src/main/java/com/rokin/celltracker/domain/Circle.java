package com.rokin.celltracker.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Circle {

  @Id
  private String id;
  private String name;
  private String code;
  private Long codeUpdateTimestamp;
  private Boolean active;
  @OneToOne
  private Client owner;
}
