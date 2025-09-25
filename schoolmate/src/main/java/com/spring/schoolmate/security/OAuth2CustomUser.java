package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Student;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class OAuth2CustomUser extends DefaultOAuth2User {

    private final Student student;

    public OAuth2CustomUser(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey, Student student) {
        super(authorities, attributes, nameAttributeKey);
        this.student = student;
    }
}
