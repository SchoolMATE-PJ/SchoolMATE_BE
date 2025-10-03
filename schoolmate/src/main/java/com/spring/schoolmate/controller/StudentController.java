package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.student.StudentRes;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j // 로깅 사용
public class StudentController {

  private final StudentService studentService;

  @GetMapping("/me")
  public ResponseEntity<StudentRes> getMyInfo(Authentication authentication) {

    log.info(">>>>>> [StudentController /me] 요청 시작"); // 진입점 로그 추가

    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn(">>>>>> [StudentController /me] UNAUTHORIZED: 인증 정보 없음.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      log.info(">>>>>> [StudentController /me] 학생 조회 시작: {}", email); // 학생 조회 시작 로그

      Student student = studentService.findByEmail(email)
        .orElseThrow(() -> new NoSuchElementException("인증된 이메일(" + email + ")에 해당하는 학생을 찾을 수 없습니다."));

      StudentRes responseDto = StudentRes.fromEntity(student);

      log.info(">>>>>> [StudentController /me] 응답 DTO 생성 완료. DTO: {}", responseDto.getEmail()); // 성공 로그

      return ResponseEntity.ok(responseDto);
    } catch (NoSuchElementException e) {
      log.error(">>>>>> [StudentController /me] NOT FOUND 오류: {}", e.getMessage()); // 오류 로그
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      // 500 오류가 여기서 잡힘. 스택 트레이스를 출력해야 함.
      log.error(">>>>>> [StudentController /me] 처리 중 치명적인 서버 오류 발생!", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}