package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter @Setter
@NoArgsConstructor
public class Role {

    // 🚨 [오류 해결] illegal start of expression 오류를 일으키는 필드 제거

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private RoleType roleName;
    public enum RoleType { STUDENT, ADMIN, TEMP_USER }
}