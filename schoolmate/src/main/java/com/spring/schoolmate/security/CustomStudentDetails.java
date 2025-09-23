package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Student;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;


@Slf4j
@Getter
public class CustomStudentDetails implements UserDetails {

    private final Student student;

    public CustomStudentDetails(Student student) {
        this.student = student;
        log.info("CustomStudentDetails===>{}",student);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       // 사용자 권한 반환
        log.info("getAuthorities() ==========>");
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> student.getRole().getRoleName().toString());
        return collection;
    }

    @Override
    public String getPassword() {
        log.info("getPassword() ===>");
        return student.getPassword();
    }

    @Override
    public String getUsername() {
        log.info("getUsername() ===>");
        return student.getEmail();
    }


}
