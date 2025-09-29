package com.spring.schoolmate.dto.auth;

import lombok.Getter;
import lombok.Setter;

// /api/auth/login 요청 본문 매핑용
@Getter
@Setter
public class LoginReq {
  private String email;
  private String password;
}