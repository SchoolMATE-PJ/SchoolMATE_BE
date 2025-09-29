package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin")
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long adminId;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password; // 암호화된 비밀번호

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role.RoleType role = Role.RoleType.ADMIN;

  @Builder
  public Admin(String email, String password, String name) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.role = Role.RoleType.ADMIN;
  }
}