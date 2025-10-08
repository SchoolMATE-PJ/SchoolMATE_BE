package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.pointhistory.PointHistoryRes;
import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.service.PointHistoryService;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/point-history")
@RequiredArgsConstructor
public class PointHistoryController {

  private final PointHistoryService pointHistoryService;
  private final StudentService studentService;

  /**
   * 로그인된 학생의 현재 보유 포인트를 조회. (authToken 기반)
   * GET /api/point-history/student/me/balance
   * @param authentication Spring Security의 인증 정보
   * @return 현재 보유 포인트 (Integer)
   */
  @GetMapping("/student/me/balance")
  public ResponseEntity<Integer> getCurrentPointBalance(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      // 2. Service를 통해 이메일 기반으로 잔액 조회
      Integer currentPoints = pointHistoryService.getCurrentBalanceByStudentEmail(email);
      return ResponseEntity.ok(currentPoints);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생을 찾지 못한 경우
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }


  /**
   * 특정 학생의 이메일을 기반으로 모든 포인트 거래 내역을 최신순으로 조회.
   * @param email 조회할 학생의 이메일
   * @return PointHistoryRes DTO 목록 (최신순)
   */
  @GetMapping("/student/{email}")
  public ResponseEntity<List<PointHistoryRes>> getPointHistoryByStudentEmail(
    @PathVariable String email) {

    try {
      List<PointHistoryRes> historyList = pointHistoryService.getHistoryByStudentEmail(email).stream()
        .map(PointHistoryRes::fromEntity)
        .collect(Collectors.toList());
      return ResponseEntity.ok(historyList);
    } catch (NoSuchElementException | NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * 포인트 거래 내역을 기록. (관리자 지급/차감 또는 내부 시스템용)
   * 상품 교환 로직은 ProductExchangeController로 분리되었으므로,
   * 이 엔드포인트는 일반적인 포인트 기록용으로만 사용됨.
   *
   * @param email   거래를 기록할 학생의 이메일
   * @param history 기록할 PointHistory 객체 (amount, tsType 등이 포함되어야 함. JSON Body)
   * @return 저장된 PointHistory 객체의 DTO와 201 Created 응답
   */
  // 엔드포인트는 유지하지만, 사용 목적이 일반 거래 기록으로 변경됨.
  @PostMapping("/student/{email}")
  public ResponseEntity<?> createPointHistoryByStudentEmail(
    @PathVariable String email,
    @RequestBody PointHistory history) {

    try {
      // Service에서 학생 존재 여부 및 유효성 검증을 수행
      PointHistory savedHistory = pointHistoryService.recordTransactionByStudentEmail(email, history);

      return ResponseEntity.status(HttpStatus.CREATED).body(PointHistoryRes.fromEntity(savedHistory));
    } catch (NoSuchElementException | NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      // 포인트 부족 등 유효성 검증 실패 시 400 BAD_REQUEST
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  /**
   * 로그인된 학생의 모든 포인트 거래 내역을 최신순으로 조회. (authToken 기반)
   * GET /api/point-history/student/me
   * @param authentication Spring Security의 인증 정보
   * @return PointHistoryRes DTO 목록 (최신순)
   */
  @GetMapping("/student/me") // <- 프론트엔드가 요청하는 경로
  public ResponseEntity<List<PointHistoryRes>> getMyPointHistory(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName(); // 토큰에서 이메일 추출

    try {
      // 이메일을 사용하여 거래 내역 조회
      List<PointHistoryRes> historyList = pointHistoryService.getHistoryByStudentEmail(email).stream()
        .map(PointHistoryRes::fromEntity)
        .collect(Collectors.toList());
      return ResponseEntity.ok(historyList);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생을 찾지 못한 경우
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * 로그인된 학생의 급식 사진 업로드 횟수를 조회. (authToken 기반)
   * GET /api/point-history/student/me/meal-count
   * @param authentication Spring Security의 인증 정보
   * @return 급식 사진 업로드 횟수 (Integer)
   */
  @GetMapping("/student/me/meal-count") // <- 프론트엔드가 요청할 경로
  public ResponseEntity<Integer> getMealPhotoUploadCount(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      // Service를 통해 이메일 기반으로 급식 사진 업로드 횟수 조회 (특정 tsType을 카운트)
      Integer count = pointHistoryService.getMealPhotoUploadCountByStudentEmail(email);
      return ResponseEntity.ok(count);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생을 찾지 못한 경우
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * 로그인된 학생이 사용한 포인트의 총합을 조회. (authToken 기반)
   * GET /api/point-history/student/me/used-points
   * @param authentication Spring Security의 인증 정보
   * @return 사용한 포인트 총합 (Integer)
   */
  @GetMapping("/student/me/used-points") // <- 프론트엔드가 요청할 경로
  public ResponseEntity<Integer> getSumOfUsedPoints(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName();

    try {
      // Service를 통해 이메일 기반으로 사용한 포인트 총합 조회
      Integer usedPoints = pointHistoryService.getSumOfUsedPointsByStudentEmail(email);
      return ResponseEntity.ok(usedPoints);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생을 찾지 못한 경우
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * 로그인된 학생의 출석을 체크하고 500 포인트를 지급.
   * 이 엔드포인트는 Spring Security의 Authentication 객체를 사용하여
   * 토큰 기반으로 인증된 사용자 본인에게만 포인트 거래 내역을 기록.
   * * POST /api/point-history/student/me/attendance
   * * @param authentication Spring Security의 인증 정보 (토큰에서 이메일 추출에 사용)
   * @return 지급된 포인트 내역(PointHistoryRes) DTO와 200 OK 응답
   */
  @PostMapping("/student/me/attendance")
  public ResponseEntity<PointHistoryRes> checkAttendance(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    // StudentService에서 이메일을 통해 ID를 찾아야 함.
    String email = authentication.getName();
    Long studentId = studentService.getStudentIdByEmail(email);

    try {
      PointHistory history = pointHistoryService.addAttendancePoint(studentId);
      return ResponseEntity.ok(PointHistoryRes.fromEntity(history));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}