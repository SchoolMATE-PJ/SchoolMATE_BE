package com.spring.schoolmate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.schoolmate.dto.auth.ExternalSignUpReq;
import com.spring.schoolmate.dto.auth.ExternalSignUpRes;
import com.spring.schoolmate.dto.external.ExternalAccountReq;
import com.spring.schoolmate.dto.student.StudentReq;
import com.spring.schoolmate.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.spring.schoolmate.dto.auth.SignUpReq;
import com.spring.schoolmate.dto.auth.SignUpRes;
import com.spring.schoolmate.dto.profile.ProfileReq;
import com.spring.schoolmate.entity.*;
import com.spring.schoolmate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthService {

    // Repository
    private final ExternalAccountRepository externalAccountRepository;

    // Service
    private final JWTUtil jwtUtil;
    private final StudentService studentService; // 주입
    private final ProfileService profileService;
    private final AllergyService allergyService;

    private final StudentRepository studentRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;

    /**
     * 일반 회원가입을 처리하는 메소드
     * 이메일, 닉네임, 전화번호 중복을 모두 검증
     */
    @Transactional
    public SignUpRes signUp(SignUpReq request) {
        // 1. 프로필 정보 중복 검사 (닉네임, 전화번호)
        profileService.duplicateCheck(request.getProfile());

        // 2. 학생 정보 저장 (StudentService에 위임)
        //    -> createStudent 내부에서 이메일 중복 검사도 함께 처리
        Student newStudent = studentService.createStudent(request.getStudent());

        // 3. 프로필 정보 저장
        Profile newProfile = profileService.registerProfile(newStudent, request.getProfile());

        // 4. 알레르기 정보 저장
        List<Allergy> allergies = allergyService.registerStudentAllergies(newStudent, request.getAllergyId());

        String token = jwtUtil.createJwt(newStudent);

        return SignUpRes.fromEntity(newStudent, newProfile, allergies, token);
    }

    @Transactional
    public ExternalSignUpRes externalSignUp(ExternalSignUpReq request) throws JsonProcessingException {
        // 1. 임시 토큰에서 소셜 정보 추출
        Map<String, Object> oauthAttributes = jwtUtil.getOAuth2AttributesFromTempToken(request.getTempToken());
        Map<String, Object> kakaoAccount = (Map<String, Object>) oauthAttributes.get("kakao_account");
        String email = kakaoAccount.get("email").toString();

        // 2. 프로필 정보 중복 검사
        profileService.duplicateCheck(request.getProfile());

        // 3. 학생 정보 저장 (StudentService에 위임)
        Student newStudent = studentService.createSocialStudent(email, request.getStudent());

        // 4. 프로필 정보 저장
        Profile newProfile = profileService.registerProfile(newStudent, request.getProfile());

        // 5. 알레르기 정보 저장
        List<Allergy> allergies = allergyService.registerStudentAllergies(newStudent, request.getAllergyId());

        // 6. 외부 계정 정보(ExternalAccount) 추가 저장
        String providerId = oauthAttributes.get("id").toString();
        ExternalAccount newExternalAccount = ExternalAccount.builder()
                .student(newStudent)
                .providerName(request.getExternalAccount().getProviderName())
                .providerId(providerId)
                .build();
        externalAccountRepository.save(newExternalAccount);

        String token = jwtUtil.createJwt(newStudent); // 로그인용 JWT 생성

        // fromEntity 메소드를 수정해서 token을 함께 반환하도록 변경
        return ExternalSignUpRes.fromEntity(newStudent, newProfile, newExternalAccount, allergies, token);
    }

    // 이메일 중복 확인 API
    @Transactional
    public boolean existCheckEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    // 닉네임 중복 확인 API
    @Transactional
    public boolean existCheckNickname(String nickname) {
        return profileRepository.existsByNickname(nickname);
    }

    // 전화번호 중복 확인 API
    @Transactional
    public boolean existCheckPhone(String phone) {
        return profileRepository.existsByPhone(phone);
    }

}
