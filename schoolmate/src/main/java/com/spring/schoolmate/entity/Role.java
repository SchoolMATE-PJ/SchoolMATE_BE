package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    // Enumerated(EnumType.STRING) :: Enum 타입을 문자열 자체로 DB에 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleName;

    // RoleType Enum으로 정의
    public enum RoleType { ADMIN, STUDENT}
}
