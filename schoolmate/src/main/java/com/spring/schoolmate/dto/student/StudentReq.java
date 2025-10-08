package com.spring.schoolmate.dto.student;

import com.spring.schoolmate.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "학생 정보 전송 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentReq {

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "비밀번호")
    private String password;

    @Schema(description = "이름")
    private String name;

    public Student toStudent(StudentReq studentReq) {
        return Student.builder()
                .email(studentReq.getEmail())
                .name(studentReq.getName())
                .build();
    }
}
