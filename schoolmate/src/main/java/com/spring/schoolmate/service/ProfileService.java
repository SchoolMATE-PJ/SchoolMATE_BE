package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.profile.ProfileReq;
import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    /**
     * 프로필 정보를 저장
     * @param student 프로필과 연관될 학생 엔티티
     * @param profileReq 프로필 정보 DTO
     * @return 저장된 프로필 엔티티
     */
    @Transactional
    public Profile registerProfile(Student student, ProfileReq profileReq) {
        Profile newProfile = Profile.builder()
                .student(student) // 엔티티 직접 참조
                .nickname(profileReq.getNickname())
                .gender(profileReq.getGender())
                .phone(profileReq.getPhone())
                .birthDay(profileReq.getBirthDay())
                .scCode(profileReq.getScCode())
                .schoolCode(profileReq.getSchoolCode())
                .schoolName(profileReq.getSchoolName())
                .studentId(student.getStudentId())
                .grade(profileReq.getGrade())
                .classNo(profileReq.getClassNo())
                .level(profileReq.getLevel())
                .profileImgUrl(profileReq.getProfileImgUrl())
                .build();
        return profileRepository.save(newProfile);
    }

    /**
     * 닉네임과 전화번호 중복을 검사합니다.
     * @param profileReq 프로필 정보 DTO
     */
    public void duplicateCheck(ProfileReq profileReq) {
        if (profileRepository.existsByNickname(profileReq.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (profileRepository.existsByPhone(profileReq.getPhone())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }
}