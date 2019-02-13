package com.rokin.celltracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFavoritePlace {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String tagName;
  private Double lat;
  private Double lon;
  private Long timestamp;
  private Boolean active;
  @ManyToOne
  @JsonIgnore
  private Client client;
}
