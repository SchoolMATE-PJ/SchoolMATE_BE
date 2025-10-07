package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.security.OAuth2CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.spring.schoolmate.exception.UserNotRegisteredException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
          .getProviderDetails()
          .getUserInfoEndpoint()
          .getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 신규 회원일 경우 여기서 예외를 던진다.
        Student student = findOrCreateStudent(registrationId, attributes);

        // 기존 회원만 아래 코드가 실행된다.
        return new OAuth2CustomUser(
          Collections.singleton(new SimpleGrantedAuthority("ROLE_STUDENT")),
          attributes,
          userNameAttributeName,
          student, // 이 시점의 student는 DB에 존재하는 기존 회원.
          registrationId
        );
    }

    private Student findOrCreateStudent(String registrationId, Map<String, Object> attributes) {
        // null 체크 추가: kakao_account나 email이 null인 경우를 방지.
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            log.error("카카오 속성(attributes)에 'kakao_account' 필드가 누락되었습니다: {}", attributes);
            throw new OAuth2AuthenticationException("카카오 계정 세부 정보를 검색할 수 없습니다.");
        }

        String email = (String) kakaoAccount.get("email");
        if (email == null) {
            log.error("카카오 계정(kakao account)에 'email' 필드가 누락되었습니다: {}", kakaoAccount);
            throw new OAuth2AuthenticationException("소셜 로그인을 위해서는 이메일이 필수입니다.");
        }


        // 1. 이메일로 DB에서 사용자를 찾아본다.
        Optional<Student> existingStudentOptional = studentRepository.findByEmail(email);

        if (existingStudentOptional.isPresent()) {
            // 2. 사용자가 이미 존재하면, 그 사용자 정보를 반환.
            log.info("기존 소셜 회원 발견: {}", email);
            return existingStudentOptional.get();
        } else {
            log.info("신규 소셜 회원 감지! UserNotRegisteredException 발생: {}", email);
            throw new UserNotRegisteredException(
              "사용자가 등록되지 않았습니다. 회원가입을 진행합니다.", // 메시지
              attributes,
              registrationId
            );
        }
    }
}