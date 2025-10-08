package com.spring.schoolmate.dto.profile;

import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "프로필 정보 전송 DTO")
public class ProfileReq {

    private Long studentId;
    
    @Schema(description = "휴대폰 번호")
    private String phone;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "성별")
    private Profile.Gender gender;

    @Schema(description = "생년월일")
    private LocalDate birthDay;

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

    @Schema(description = "반 번호")
    private Integer classNo;

    @Schema(description = "교육 수준")
    private String level;

    private String profileImgUrl;

    public Profile toProfile(ProfileReq profileReq) {
        return Profile.builder()
                .student(Student.builder().studentId(profileReq.getStudentId()).build())
                .phone(profileReq.getPhone())
                .nickname(profileReq.getNickname())
                .gender(profileReq.getGender())
                .birthDay(profileReq.getBirthDay())
                .scCode(profileReq.getScCode())
                .schoolCode(profileReq.getSchoolCode())
                .schoolName(profileReq.getSchoolName())
                .majorName(profileReq.getMajorName())
                .grade(profileReq.getGrade())
                .classNo(profileReq.getClassNo())
                .level(profileReq.getLevel())
                .profileImgUrl(profileReq.getProfileImgUrl())
                .build();
    }

}
