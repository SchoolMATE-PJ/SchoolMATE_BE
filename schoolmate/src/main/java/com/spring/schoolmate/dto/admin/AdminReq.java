package com.spring.schoolmate.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // 요청 DTO는 ObjectMapper가 값을 설정할 수 있도록 Setter나 AllArgsConstructor가 필요.
public class AdminReq {
  private String email;
  private String password;
  private String name;
}