package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Student;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class OAuth2CustomUser extends DefaultOAuth2User {

    private final Student student;
    private final String registrationId; // OAuth2 제공자 ID (예: kakao, naver)

    // 🚨 [오류 해결] 생성자에 5가지 인수를 모두 받습니다.
    public OAuth2CustomUser(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            Student student,
                            String registrationId) {

        super(authorities, attributes, nameAttributeKey);
        this.student = student;
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }
}