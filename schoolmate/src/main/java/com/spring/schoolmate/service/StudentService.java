package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.student.StudentReq;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에서 기본적으로 읽기 전용 트랜잭션 설정
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 학생(Student) 정보를 저장
     * @param request 학생 정보 DTO
     * @return 저장된 학생 엔티티
     */
    @Transactional // 쓰기 작업은 개별적으로 @Transactional 명시
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
     * @throws IllegalArgumentException 이미 사용 중인 이메일인 경우 예외 발생
     */
    public void duplicateCheckByEmail(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 이메일로 학생 정보를 조회
     * @param email 조회할 이메일
     * @return 조회된 Student 엔티티 (Optional<Student>)
     */
    public Optional<Student> findByEmail(String email) {
        // @Transactional(readOnly = true)가 클래스 레벨에 적용되어 있다.
        // StudentRepository에 정의된 findByEmail을 사용하여 Optional<Student>를 반환
        return studentRepository.findByEmail(email);
    }
}