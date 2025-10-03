package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.pointhistory.PointHistoryRes;
import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.service.PointHistoryService;
import com.spring.schoolmate.exception.NotFoundException;
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

  /**
   * 로그인된 학생의 현재 보유 포인트를 조회. (authToken 기반)
   * GET /api/point-history/student/me/balance
   * @param authentication Spring Security의 인증 정보
   * @return 현재 보유 포인트 (Integer)
   */
  @GetMapping("/student/me/balance") // 엔드포인트 수정: /student/{email}/balance -> /student/me/balance
  public ResponseEntity<Integer> getCurrentPointBalance(Authentication authentication) {
    // 1. 인증 정보에서 이메일(Principal)을 추출
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = authentication.getName(); // Spring Security Principal은 이메일(사용자명)을 반환

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
   * role이 'STUDENT'인 학생의 이메일을 기반으로 포인트 거래 내역을 기록.
   * @param email 거래를 기록할 학생의 이메일
   * @param history 기록할 PointHistory 객체 (amount, tsType 등이 포함되어야 함. JSON Body)
   * @return 저장된 PointHistory 객체와 201 Created 응답
   */
  @PostMapping("/student/{email}")
  public ResponseEntity<PointHistoryRes> createPointHistoryByStudentEmail(
    @PathVariable String email,
    @RequestBody PointHistory history) {

    try {
      PointHistory savedHistory = pointHistoryService.recordTransactionByStudentEmail(email, history);

      // 엔티티 대신 DTO를 사용하여 응답
      return ResponseEntity.status(HttpStatus.CREATED).body(PointHistoryRes.fromEntity(savedHistory));
    } catch (NoSuchElementException | NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}