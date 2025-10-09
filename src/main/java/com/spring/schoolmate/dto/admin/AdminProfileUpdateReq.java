package com.spring.schoolmate.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProfileUpdateReq {
  private String name;    // Student 엔티티의 이름
  private String phone;   // Profile 엔티티의 휴대폰 번호
  private Integer grade;  // Profile 엔티티의 학년
  private Integer classNo; // Profile 엔티티의 반 번호
  private Integer points;  // 최종 목표 보유 포인트 (Student.pointBalance에 반영될 값)
}