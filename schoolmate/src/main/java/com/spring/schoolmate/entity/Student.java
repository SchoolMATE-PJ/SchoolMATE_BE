package com.spring.schoolmate.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student")
public class Student {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    @JsonIgnore // JSON 순환 참조 방지를 위해 추가
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore
    private Role role;

    // --- [추가] StudentAllergy와의 관계 설정 ---
    // 한 명의 학생(Student)은 여러 개의 학생-알레르기(StudentAllergy) 정보를 가질 수 있다.
    // CascadeType.ALL: 학생 정보가 저장/삭제될 때, 관련된 알레르기 정보도 함께 처리
    // orphanRemoval = true: 학생에게서 특정 알레르기 정보가 제거되면 DB에서도 삭제
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 이 필드를 null이 아닌 빈 리스트로 초기화
    private List<StudentAllergy> studentAllergies = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // 🚨 [오류 해결] OAuth2 provider 필드 추가 (cannot find symbol method provider 해결)
    @Column(name = "provider")
    private String provider; // CustomOAuth2UserService에서 사용 가능

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "point_balance", nullable = false)
    @Builder.Default
    private Integer pointBalance = 0;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}