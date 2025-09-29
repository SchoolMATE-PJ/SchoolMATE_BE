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

        // 🚨 [오류 해결] getNameAttributeKey 대신 userRequest에서 추출
        String userNameAttributeName = userRequest.getClientRegistration()
          .getProviderDetails()
          .getUserInfoEndpoint()
          .getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Student student = findOrCreateStudent(registrationId, attributes);

        // 🚨 [오류 해결] OAuth2CustomUser 생성 시 5가지 인수를 모두 전달
        return new OAuth2CustomUser(
          Collections.singleton(new SimpleGrantedAuthority("ROLE_STUDENT")),
          attributes,
          userNameAttributeName,
          student,
          registrationId
        );
    }

    private Student findOrCreateStudent(String registrationId, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");

        // 🚨 [오류 해결] RoleType 대신 Role 엔티티를 조회하여 주입 (incompatible types 해결)
        // RoleRepository의 findByRoleName(Role.RoleType) 메서드가 있다고 가정합니다.
        Role studentRole = roleRepository.findByRoleName(Role.RoleType.STUDENT)
          .orElseThrow(() -> new RuntimeException("STUDENT Role not found"));

        // 신규 사용자 또는 임시 사용자 객체 반환
        return Student.builder()
          .email(email)
          .role(studentRole) // Role 엔티티 주입
          .provider(registrationId) // 🚨 Student.java 수정으로 해결
          .name("임시사용자")
          .build();
    }
}