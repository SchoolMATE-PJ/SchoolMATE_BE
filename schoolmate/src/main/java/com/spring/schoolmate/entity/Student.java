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
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // updatable = false :: 업데이트 불가
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    // UpdateTimestamp :: 업데이트 시 Hibernate가 자동으로 시간 넣어줌
    @UpdateTimestamp
    private LocalDateTime updateAt;

    private LocalDateTime lastLoginAt;

    @Column(name = "point_balance", nullable = false)
    @Builder.Default // Lombok의 Builder 패턴에서 필드 초기값을 명시적으로 다루는 표준적인 방법
    private Integer pointBalance = 0; // 이 필드에 @Builder.Default 추가
}

