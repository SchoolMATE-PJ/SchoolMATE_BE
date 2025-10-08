package com.spring.schoolmate.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordUpdateReq {
    @Schema(description = "현재 비밀번호", example = "currentPassword123!")
    private String currentPassword;

    @Schema(description = "새로운 비밀번호", example = "newPassword123!")
    private String newPassword;
}
