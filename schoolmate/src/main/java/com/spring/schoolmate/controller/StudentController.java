package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.student.PasswordUpdateReq;
import com.spring.schoolmate.dto.student.StudentRes;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j // 로깅 사용
@Tag(name = "Students", description = "학생(사용자) 관련 API")
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
    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal CustomStudentDetails customStudentDetails,
            @RequestBody PasswordUpdateReq request) {

        Long currentStudentId = customStudentDetails.getStudent().getStudentId();
        studentService.updatePassword(currentStudentId, request);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자 본인의 계정을 삭제합니다.")
    public ResponseEntity<String> deleteMyAccount(
            @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        Long currentStudentId = customStudentDetails.getStudent().getStudentId();
        studentService.deleteStudent(currentStudentId);

        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }

}