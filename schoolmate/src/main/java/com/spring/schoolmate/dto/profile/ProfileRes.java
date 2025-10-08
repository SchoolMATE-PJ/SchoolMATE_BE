package com.spring.schoolmate.dto.profile;

import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Profile.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프로필 정보 응답 DTO")
public class ProfileRes {
    private Long studentId;
    private String nickname;
    private Gender gender;
    private String phone;
    private LocalDate birthDay;
    private String scCode;
    private String majorName;
    private String schoolCode;
    private String schoolName;
    private Integer grade;
    private String level;
    private Integer classNo;
    private String profileImgUrl;

    public static ProfileRes fromEntity(Profile profile) {
        return ProfileRes.builder()
                .studentId(profile.getStudentId())
                .nickname(profile.getNickname())
                .gender(profile.getGender())
                .phone(profile.getPhone())
                .birthDay(profile.getBirthDay())
                .scCode(profile.getScCode())
                .schoolCode(profile.getSchoolCode())
                .schoolName(profile.getSchoolName())
                .majorName(profile.getMajorName())
                .grade(profile.getGrade())
                .level(profile.getLevel())
                .classNo(profile.getClassNo())
                .profileImgUrl(profile.getProfileImgUrl())
                .build();
    }
}
