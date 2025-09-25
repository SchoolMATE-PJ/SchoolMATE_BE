package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.ExternalAccount;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ExternalAccountRepository;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.security.OAuth2CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final StudentRepository studentRepository;
    private final ExternalAccountRepository externalAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "kakao"

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String providerId = attributes.get("id").toString();
        String email = kakaoAccount.get("email").toString();

        // [시나리오 1] 카카오 ID로 이미 가입된 소셜 회원인지 확인
        Optional<ExternalAccount> externalAccountOptional = externalAccountRepository
                .findByProviderNameAndProviderId(provider, providerId);

        if (externalAccountOptional.isPresent()) {
            log.info("기존 소셜 회원으로 로그인합니다. Provider ID: {}", providerId);
            Student student = externalAccountOptional.get().getStudent();
            return createOAuth2CustomUser(oAuth2User, student);
        }

        // [시나리오 2] 카카오 이메일로 일반 가입한 회원이 있는지 확인
        Optional<Student> studentOptional = studentRepository.findByEmail(email);
        if (studentOptional.isPresent()) {
            log.info("기존 일반 회원을 소셜 계정과 자동 연동합니다. Email: {}", email);
            Student student = studentOptional.get();
            // DB에 소셜 계정 정보를 새로 연결(저장)
            ExternalAccount newExternalAccount = ExternalAccount.builder()
                    .student(student)
                    .providerName(provider)
                    .providerId(providerId)
                    .build();
            externalAccountRepository.save(newExternalAccount);
            return createOAuth2CustomUser(oAuth2User, student);
        }

        // [시나리오 3] 위 두 경우 모두 아니면, 완전 신규 회원으로 판단
        log.info("신규 소셜 회원입니다. 추가 정보 입력 페이지로 이동이 필요합니다.");
        // DB에 저장하지 않고, SuccessHandler에서 신규 회원임을 판단할 수 있도록 임시 Student 객체를 생성
        Student tempStudent = Student.builder().name("GUEST_FOR_SIGNUP").build();
        return createOAuth2CustomUser(oAuth2User, tempStudent);
    }

    private OAuth2CustomUser createOAuth2CustomUser(OAuth2User oAuth2User, Student student) {
        return new OAuth2CustomUser(
                Collections.emptyList(),
                oAuth2User.getAttributes(),
                "id", // 카카오의 경우 'id'가 고유 식별자
                student
        );
    }
}
