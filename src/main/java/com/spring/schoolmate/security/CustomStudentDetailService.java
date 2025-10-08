package com.spring.schoolmate.security;

import com.spring.schoolmate.entity.Admin; // 🚨 [추가] Admin 엔티티 임포트
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.AdminRepository; // 🚨 [추가] AdminRepository 임포트
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
    private final AdminRepository adminRepository; // 🚨 [추가] AdminRepository 주입

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("UserDetailsService loadUserByUsername() call....email {}", email);

        // 1. Student 계정 조회 시도
        if (email.contains("@")) { // 이메일 형식을 가진 경우 (Student 또는 Admin)
            Student findStudent = studentRepository.findByEmail(email).orElse(null);

            if (findStudent != null) {
                log.info("findStudent ====> {}", findStudent.getEmail());
                return new CustomStudentDetails(findStudent);
            }

            // 2. Student가 아니면 Admin 계정 조회 시도
            Admin findAdmin = adminRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

            log.info("findAdmin ====> {}", findAdmin.getEmail());
            return new CustomAdminDetails(findAdmin); // 🚨 [변경] CustomAdminDetails 반환

        } else {
            // 이메일 형식이 아닌 다른 식별자라면 Student만 조회한다고 가정 (선택 사항)
            throw new UsernameNotFoundException("유효한 이메일 형식이 아닙니다: " + email);
        }
    }
}