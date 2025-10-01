package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.repository.RoleRepository; // 🚨 RoleRepository 임포트 추가
import com.spring.schoolmate.security.OAuth2CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository; // 🚨 RoleRepository 주입

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // getNameAttributeKey 대신 userRequest에서 추출
        String userNameAttributeName = userRequest.getClientRegistration()
          .getProviderDetails()
          .getUserInfoEndpoint()
          .getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Student student = findOrCreateStudent(registrationId, attributes);

        // OAuth2CustomUser 생성 시 5가지 인수를 모두 전달
        return new OAuth2CustomUser(
          Collections.singleton(new SimpleGrantedAuthority("ROLE_STUDENT")),
          attributes,
          userNameAttributeName,
          student,
          registrationId
        );
    }

    private Student findOrCreateStudent(String registrationId, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        // 1. 이메일로 DB에서 사용자를 찾아봅니다.
        Optional<Student> existingStudentOptional = studentRepository.findByEmail(email);

        if (existingStudentOptional.isPresent()) {
            // 2. 사용자가 이미 존재하면, 그 사용자 정보를 반환합니다.
            log.info("기존 소셜 회원 발견: {}", email);
            return existingStudentOptional.get();
        } else {
            // 3. 사용자가 존재하지 않으면, '신규 회원'으로 판단하고
            //    추가 정보 입력을 위한 임시 Student 객체를 생성하여 반환합니다.
            //    이 객체는 아직 DB에 저장되지 않았으므로 studentId가 null입니다.
            log.info("신규 소셜 회원! 추가 정보 입력이 필요합니다: {}", email);
            Role studentRole = roleRepository.findByRoleName(Role.RoleType.STUDENT)
                    .orElseThrow(() -> new RuntimeException("STUDENT Role not found"));

            return Student.builder()
                    .email(email)
                    .role(studentRole)
                    .provider(registrationId) // "kakao"
                    .build();
        }
    }
}