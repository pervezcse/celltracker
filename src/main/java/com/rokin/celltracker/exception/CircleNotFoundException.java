package com.rokin.celltracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CircleNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public CircleNotFoundException(String message) {
    super(message);
  }
}
