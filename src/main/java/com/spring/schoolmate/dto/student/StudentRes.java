package com.spring.schoolmate.dto.student;

import com.spring.schoolmate.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "학생 정보 응답 DTO")
public class StudentRes {
    private Long studentId;
    private String email;
    private String name;
    private Integer pointBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    public static StudentRes fromEntity(Student student) {
        return StudentRes.builder()
                .studentId(student.getStudentId())
                .email(student.getEmail())
                .name(student.getName())
                .pointBalance(student.getPointBalance())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
