package com.spring.schoolmate.dto.profile;

import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.StudentAllergy;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Slf4j
public class MyProfileRes {

    // --- Student 정보 ---
    private Long studentId;
    private String email;
    private String name;

    // --- Profile 정보 ---
    private String nickname;
    private Profile.Gender gender;
    private String phone;
    private LocalDate birthDay;
    private String profileImgUrl;
    private String schoolName;
    private Integer grade;
    private Integer classNo;

    // --- 알레르기 정보 ---
    private List<Integer> allergyId;

    // Entity -> DTO 변환 메소드
    public static MyProfileRes fromEntity(Profile profile) {
        // 학생의 알레르기 ID 목록을 추출
        List<Integer> allergyIds = profile.getStudent().getStudentAllergies().stream()
                .map(studentAllergy -> studentAllergy.getAllergy().getAllergyId())
                .collect(Collectors.toList());

        return MyProfileRes.builder()
                // Student 정보 매핑
                .studentId(profile.getStudent().getStudentId())
                .email(profile.getStudent().getEmail())
                .name(profile.getStudent().getName())
                // Profile 정보 매핑
                .nickname(profile.getNickname())
                .gender(profile.getGender())
                .phone(profile.getPhone())
                .birthDay(profile.getBirthDay())
                .profileImgUrl(profile.getProfileImgUrl())
                .schoolName(profile.getSchoolName())
                .grade(profile.getGrade())
                .classNo(profile.getClassNo())
                // 알레르기 정보 매핑
                .allergyId(allergyIds)
                .build();
    }
}