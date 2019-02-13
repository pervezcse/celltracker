package com.rokin.celltracker.dto;

import com.rokin.celltracker.domain.Circle;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CircleInfo {
  private final Circle circle;
  private final Integer mumberCount;
}
