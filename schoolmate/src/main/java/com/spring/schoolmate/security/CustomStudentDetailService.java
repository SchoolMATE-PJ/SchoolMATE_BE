package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomStudentDetailService implements UserDetailsService {

    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String studentEmail) throws UsernameNotFoundException {
        log.info("UserDetailsService loadUserByUsername() call....studentEmail {}", studentEmail);
        Student findStudent = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + studentEmail));

        log.info("findStudent ====> {}",findStudent);
        return new CustomStudentDetails(findStudent);
    }
}
