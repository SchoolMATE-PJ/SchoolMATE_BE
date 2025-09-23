package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.auth.ExternalSignUpReq;
import com.spring.schoolmate.dto.auth.ExternalSignUpRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.spring.schoolmate.dto.auth.SignUpReq;
import com.spring.schoolmate.dto.auth.SignUpRes;
import com.spring.schoolmate.dto.profile.ProfileReq;
import com.spring.schoolmate.entity.*;
import com.spring.schoolmate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthService {

    private final StudentRepository studentRepository;
    private final ProfileRepository profileRepository;
    private final AllergyRepository allergyRepository;
    private final StudentAllergyRepository studentAllergyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExternalAccountRepository externalAccountRepository;


    /**
     * 일반 회원가입 처리
     * @param request SignUpReq (student, profile, allergyId)
     * @return SignUpRes
     */
    @Transactional
    public SignUpRes signUp(SignUpReq request) {
        // 공통 회원가입 로직 호출
        Student newStudent = registerStudent(request.getStudent().getEmail(), request.getStudent().getPassword(), request.getStudent().getName());
        Profile newProfile = registerProfile(newStudent, request.getProfile());
        List<Allergy> allergies = registerAllergy(newStudent, request.getAllergyId());

        // DTO로 변환하여 반환
        return SignUpRes.fromEntity(newStudent, newProfile, allergies);
    }

    /**
     * 소셜 회원가입 처리
     * @param request ExternalSignUpReq (student, profile, allergyId, externalAccount)
     * @return ExternalSignUpRes
     */
    @Transactional
    public ExternalSignUpRes externalSignUp(ExternalSignUpReq request) {
        // 공통 회원가입 로직 호출
        Student newStudent = registerStudent(request.getStudent().getEmail(), request.getStudent().getPassword(), request.getStudent().getName());
        Profile newProfile = registerProfile(newStudent, request.getProfile());
        List<Allergy> allergies = registerAllergy(newStudent, request.getAllergyId());

        // 소셜 계정 정보 저장
        ExternalAccount newExternalAccount = ExternalAccount.builder()
                .student(newStudent)
                .providerName(request.getExternalAccount().getProviderName())
                .providerId(request.getExternalAccount().getProviderId())
                .build();
        externalAccountRepository.save(newExternalAccount);

        // DTO로 변환하여 반환
        return ExternalSignUpRes.fromEntity(newStudent, newProfile, newExternalAccount, allergies);
    }

    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일 중복 여부를 확인합니다.")
    public void duplicateCheckByEmail(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    @Operation(summary = "전화번호 중복 확인", description = "회원가입 시 전화번호 중복 여부를 확인합니다.")
    public void duplicateCheckByPhone(String phone) {
        if (profileRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }

    // 일반 & 소셜 회원가입 공통
    @Operation(summary = "학생 등록", description = "일반 및 소셜 회원가입 시 학생 정보를 등록합니다.")
    private Student registerStudent(String email, String password, String name) {
        // 이메일 중복 확인
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 기본 권한(ROLE_STUDENT) 조회
        Role studentRole = roleRepository.findByRoleName(Role.RoleType.STUDENT)
                .orElseThrow(() -> new RuntimeException("학생 권한을 찾을 수 없습니다."));

        // Student Entity 생성
        Student newStudent = Student.builder()
                .email(email)
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화
                .name(name)
                .role(studentRole)
                .pointBalance(0) // 초기 포인트 0임
                .build();

        // 4. DB에 저장
        return studentRepository.save(newStudent);
    }

    @Operation(summary = "프로필 등록", description = "학생의 상세 프로필 정보를 등록합니다.")
    private Profile registerProfile(Student student, ProfileReq profileReq) {
        // 전화번호 중복 확인
        if (profileRepository.existsByPhone(profileReq.getPhone())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }

        // 닉네임 중복 확인
        if (profileRepository.existsByNickname(profileReq.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        Profile newProfile = Profile.builder()
                .student(student)
                .nickname(profileReq.getNickname())
                .gender(profileReq.getGender())
                .phone(profileReq.getPhone())
                .birthDay(profileReq.getBirthDay())
                .scCode(profileReq.getScCode())
                .schoolCode(profileReq.getSchoolCode())
                .schoolName(profileReq.getSchoolName())
                .grade(profileReq.getGrade())
                .classNo(profileReq.getClassNo())
                .level(profileReq.getLevel())
                .build();

        return profileRepository.save(newProfile);
    }

    @Operation(summary = "알레르기 등록", description = "학생의 알레르기 정보를 등록합니다.")
    private List<Allergy> registerAllergy(Student student, List<Integer> allergyIds) {
        if (allergyIds == null || allergyIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Allergy> allergies = allergyRepository.findAllById(allergyIds);
        List<StudentAllergy> studentAllergies = allergies.stream()
                .map(allergy -> new StudentAllergy(student, allergy))
                .collect(Collectors.toList());
        studentAllergyRepository.saveAll(studentAllergies);

        return allergies;
    }
}
