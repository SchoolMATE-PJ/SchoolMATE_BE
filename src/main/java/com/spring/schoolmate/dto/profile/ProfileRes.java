package com.spring.schoolmate.dto.profile;

import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Profile.Gender;
import com.spring.schoolmate.entity.Student;
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

    // 관리자 페이지 학생 정보를 불러오기 위한 필드 추가
    private String name; // Student 엔터티의 name
    private Integer pointBalance; // Student 엔터티의 pointBalance

    public static ProfileRes fromEntity(Profile profile) {

        Student student = profile.getStudent();

        return ProfileRes.builder()
                // Student 엔터티에서 name과 pointBalance를 가져옴
                .name(student.getName())
                .pointBalance(student.getPointBalance())
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
