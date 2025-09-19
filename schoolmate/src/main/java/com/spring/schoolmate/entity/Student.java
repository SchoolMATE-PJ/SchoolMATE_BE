package com.spring.schoolmate.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student")
public class Student {

    // 학생 고유 ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    // 권한 :: Role Table PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // 이메일 :: 중복 허용하지 않는다.
    @Column(nullable = false, unique = true)
    private String email;

    // 비밀번호 :: 암호화
    @Column(nullable = false)
    private String password;

    // 이름
    @Column(nullable = false)
    private String name;

    // 회원 가입 일자
    // updatable = false :: 업데이트 불가
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 회원 정보 수정 시간
    // UpdateTimestamp :: 업데이트 시 Hibernate가 자동으로 시간 넣어줌
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 마지막 로그인 시간
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 포인트 금액 :: Default 0
    @Column(name = "point_balance", nullable = false)
    @Builder.Default // Lombok의 Builder 패턴에서 필드 초기값을 명시적으로 다루는 표준적인 방법
    private Integer pointBalance = 0; // 이 필드에 @Builder.Default 추가
}