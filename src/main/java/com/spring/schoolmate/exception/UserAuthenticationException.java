package com.spring.schoolmate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAuthenticationException extends RuntimeException {
  private String message;
  private String title;
  private HttpStatus httpStatus;

  public UserAuthenticationException(String message, String title, HttpStatus httpStatus) {
    super(message);
    this.message = message;
    this.title = title;
    this.httpStatus = httpStatus;
  }
  public UserAuthenticationException(String message, String title) {
    this(message, title,HttpStatus.EXPECTATION_FAILED);
  }
}
