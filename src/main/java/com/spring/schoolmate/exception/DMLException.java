package com.spring.schoolmate.exception;

import lombok.Getter;

@Getter
public class DMLException extends RuntimeException {
  public DMLException(String message) {
    super(message);
  }
}


