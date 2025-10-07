package com.spring.schoolmate.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

@Getter
public class UserNotRegisteredException extends AuthenticationException {

  private final Map<String, Object> attributes;
  private final String provider;

  public UserNotRegisteredException(String msg, Map<String, Object> attributes, String provider) {
    super(msg);
    this.attributes = attributes;
    this.provider = provider;
  }

  // 기존 생성자 유지
  public UserNotRegisteredException(String msg, Throwable cause, Map<String, Object> attributes, String provider) {
    super(msg, cause);
    this.attributes = attributes;
    this.provider = provider;
  }
}