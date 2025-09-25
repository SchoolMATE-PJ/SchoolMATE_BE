package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.student.StudentReq;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 학생(Student) 정보를 저장
     * @param request 학생 정보 DTO
     * @return 저장된 학생 엔티티
     */
    @Transactional
    public Student createStudent(StudentReq request) {
        // 이메일 중복 검사를 먼저 수행
        duplicateCheckByEmail(request.getEmail());

        // 권한 정보 조회
        Role studentRole = roleRepository.findByRoleName(Role.RoleType.STUDENT)
                .orElseThrow(() -> new NoSuchElementException("STUDENT 역할이 DB에 존재하지 않습니다."));

        Student newStudent = Student.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(studentRole)
                .build();
        return studentRepository.save(newStudent);
    }

    /**
     * 소셜 로그인용 학생(Student) 정보를 저장합니다.
     * @param email 소셜 계정 이메일
     * @param request 학생 정보 DTO (비밀번호, 이름)
     * @return 저장된 학생 엔티티
     */
    @Transactional
    public Student createSocialStudent(String email, StudentReq request) {
        duplicateCheckByEmail(email);
        Role studentRole = roleRepository.findByRoleName(Role.RoleType.STUDENT)
                .orElseThrow(() -> new NoSuchElementException("STUDENT 역할이 DB에 존재하지 않습니다."));

        Student newStudent = Student.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(studentRole)
                .build();
        return studentRepository.save(newStudent);
    }

    /**
     * 이메일 중복 체크
     * @param email 검사할 이메일
     */
    public void duplicateCheckByEmail(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }
}
