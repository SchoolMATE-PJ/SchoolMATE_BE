package com.spring.schoolmate.dto.profile;

import com.spring.schoolmate.entity.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "프로필 정보 수정 요청 DTO")
public class ProfileUpdateReq {

    @Schema(description = "새 닉네임", example = "새로운별명123")
    private String nickname;

    @Schema(description = "성별", example = "MALE, FEMALE")
    private Profile.Gender gender;

    @Schema(description = "새 휴대폰 번호")
    private String phone;

    @Schema(description = "생년월일", example = "2007-05-10")
    private LocalDate birthDay;

    @Schema(description = "새 프로필 이미지 URL")
    private String profileImgUrl;

    // --- 학교 정보 ---
    @Schema(description = "시도교육청코드")
    private String scCode;

    @Schema(description = "학교 행정표준코드")
    private String schoolCode;

    @Schema(description = "학교명")
    private String schoolName;

    @Schema(description = "학과명")
    private String majorName;

    @Schema(description = "학년")
    private Integer grade;

    @Schema(description = "교육 수준")
    private String level;

    @Schema(description = "반 번호")
    private Integer classNo;
}
