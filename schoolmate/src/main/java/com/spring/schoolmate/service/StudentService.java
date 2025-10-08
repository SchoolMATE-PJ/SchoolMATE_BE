package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.student.PasswordUpdateReq;
import com.spring.schoolmate.dto.student.StudentReq;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.RoleRepository;
import com.spring.schoolmate.repository.StudentAllergyRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에서 기본적으로 읽기 전용 트랜잭션 설정
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final StudentAllergyRepository studentAllergyRepository;

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

    // 이메일로 학생 ID(PK)를 조회
    /**
     * 이메일로 학생의 고유 ID(PK)를 조회.
     * @param email 조회할 이메일
     * @return 학생의 ID (Long)
     * @throws NoSuchElementException 해당 이메일의 학생이 없을 경우
     */
    public Long getStudentIdByEmail(String email) {
        // findByEmail을 사용하여 학생 엔티티를 찾고, 존재하지 않으면 예외를 발생시킵니다.
        Student student = findByEmail(email)
          .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

        // 찾은 학생 엔티티에서 ID를 추출하여 반환합니다.
        return student.getStudentId();
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param studentId 현재 로그인된 사용자의 ID
     * @param request 비밀번호 변경 요청 DTO
     */
    @Transactional
    public void updatePassword(Long studentId, PasswordUpdateReq request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("학생 정보를 찾을 수 없습니다."));

        // 1. 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), student.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호로 변경 후 저장 (엔티티의 setter 호출 후 더티 체킹으로 자동 업데이트)
        student.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 학생 정보를 삭제 (회원 탈퇴)
     * @param studentId 삭제할 학생의 ID
     */
    @Transactional
    public void deleteStudent(Long studentId) {

        log.info(">>>>> 회원 탈퇴 서비스 시작 - Student ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("회원 탈퇴 실패: 존재하지 않는 학생 ID {}", studentId);
                    return new NoSuchElementException("학생 정보를 찾을 수 없습니다.");
                });

        // 1. 연관된 알레르기 정보 삭제
        log.info("... StudentAllergy 정보 삭제 Student ID: {}", studentId);
        studentAllergyRepository.deleteAllByStudent(student);
        log.info("... StudentAllergy 정보 삭제 완료. Student ID: {}", studentId);

        // 2. 연관된 프로필 정보 삭제
        log.info("... Profile 정보 삭제 중... Student ID: {}", studentId);
        profileRepository.deleteById(studentId);
        log.info("... Profile 정보 삭제 완료. Student ID: {}", studentId);

        // 3. 학생(계정) 정보 최종 삭제
        log.info("... Student 계정 정보 삭제 중... Student ID: {}", studentId);
        studentRepository.delete(student);
        log.info("... Student 계정 정보 삭제 완료. Student ID: {}", studentId);

        log.info("<<<<< 회원 탈퇴 서비스 성공 - Student ID: {}", studentId);
    }

}