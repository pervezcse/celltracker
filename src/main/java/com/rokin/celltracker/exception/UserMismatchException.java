package com.rokin.celltracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserMismatchException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public UserMismatchException(String message) {
    super(message);
  }
}
