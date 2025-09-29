package com.spring.schoolmate.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRes {
  private Long adminId;
  private String email;
  private String name;
  private String role; // RoleType.ADMIN
}