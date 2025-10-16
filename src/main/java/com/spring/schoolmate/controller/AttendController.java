package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.pointhistory.PointHistoryRes;
import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.service.PointHistoryService;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.service.StudentService;
import io.swagger.v3.oas.annotations.Operation; // Operation 어노테이션 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 학생 출석 관리 컨트롤러.
 * 출석 체크(포인트 지급) 및 출석 일수/날짜 조회 기능을 제공합니다.
 */
@Tag(name = "Attendance", description = "학생 출석 체크(포인트 지급) 및 출석 기록 조회 API")
@RestController
@RequestMapping("/api/attend") // 기본 경로를 /api/attend로 설정
@RequiredArgsConstructor
public class AttendController {

  private final PointHistoryService pointHistoryService;
  private final StudentService studentService;

  /**
   * 로그인된 학생의 오늘 출석을 체크하고 500 포인트를 지급합니다.
   * POST /api/attend/student/me/check
   * @param authentication Spring Security의 인증 정보
   * @return 지급된 포인트 내역(PointHistoryRes) DTO
   */
  @Operation(
    summary = "오늘의 출석 체크 및 포인트 지급",
    description = "로그인한 학생의 오늘 출석을 기록하고 정해진 포인트(예: 500P)를 지급합니다. 하루에 한 번만 가능합니다."
  )
  @PostMapping("/student/me/check")
  public ResponseEntity<PointHistoryRes> checkAttendance(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
    }
    String email = authentication.getName();
    // 실제 로직에서는 이메일을 통해 studentId를 조회해야 함
    Long studentId = studentService.getStudentIdByEmail(email);

    try {
      // Service에서 오늘 출석 여부, 포인트 지급 및 기록 처리
      PointHistory history = pointHistoryService.addAttendancePoint(studentId);
      return ResponseEntity.ok(PointHistoryRes.fromEntity(history)); // 200 OK
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 학생 없음
    } catch (IllegalStateException e) {
      // '오늘 이미 출석 완료' 등 비즈니스 로직 오류
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 BAD REQUEST
    }
  }

  /**
   * 로그인된 학생의 전체 누적 출석 일수를 조회합니다.
   * GET /api/attend/student/me/count
   * @param authentication Spring Security의 인증 정보
   * @return 누적 출석 일수 (Integer)
   */
  @Operation(
    summary = "전체 누적 출석 일수 조회",
    description = "로그인한 학생의 서비스 이용 기간 동안의 총 출석 횟수를 조회합니다."
  )
  @GetMapping("/student/me/count")
  public ResponseEntity<Integer> getAttendanceCount(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      Integer count = pointHistoryService.getAttendanceCountByStudentEmail(email);
      return ResponseEntity.ok(count);
    } catch (NoSuchElementException | NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * 로그인된 학생의 특정 연/월에 출석한 날짜 리스트를 조회합니다.
   * GET /api/attend/student/me/dates?year={year}&month={month}
   * @param authentication Spring Security의 인증 정보
   * @param year 조회할 연도
   * @param month 조회할 월
   * @return 해당 월에 출석한 날짜 문자열 리스트 (예: ["2025-09-01", "2025-09-02", ...])
   */
  @Operation(
    summary = "특정 월의 출석 날짜 리스트 조회",
    description = "로그인한 학생이 지정된 연도(year)와 월(month)에 출석한 날짜(YYYY-MM-DD 형식) 리스트를 조회합니다. 주로 달력 표시용으로 사용됩니다."
  )
  @GetMapping("/student/me/dates")
  public ResponseEntity<List<String>> getAttendanceDatesByMonth(
    Authentication authentication,
    @RequestParam int year,
    @RequestParam int month) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      // Service에서 해당 월의 출석 기록 리스트를 가져와 String List로 변환
      List<String> dates = pointHistoryService.getAttendanceDatesByStudentEmailAndMonth(email, year, month);
      return ResponseEntity.ok(dates);
    } catch (NoSuchElementException | NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}