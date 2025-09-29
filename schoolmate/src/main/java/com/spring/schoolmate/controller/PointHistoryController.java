package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.service.PointHistoryService;
import com.spring.schoolmate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/point-history")
@RequiredArgsConstructor
public class PointHistoryController {

  private final PointHistoryService pointHistoryService;

  /**
   * 특정 학생의 이메일을 기반으로 모든 포인트 거래 내역을 최신순으로 조회.
   * @param email 조회할 학생의 이메일
   * @return PointHistory 엔티티 목록 (최신순)
   */
  @GetMapping("/student/{email}")
  public ResponseEntity<List<PointHistory>> getPointHistoryByStudentEmail(
    @PathVariable String email) {

    try {
      List<PointHistory> historyList = pointHistoryService.getHistoryByStudentEmail(email);
      // PointHistoryService에서 NoSuchElementException 대신 NotFoundException을 사용했으나,
      // 통일성을 위해 둘 다 처리하거나, 서비스 계층에서 RuntimeException으로 통일하는 것이 좋다.
      return ResponseEntity.ok(historyList);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생 이메일이 유효하지 않은 경우 404 Not Found 처리
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
  public ResponseEntity<PointHistory> createPointHistoryByStudentEmail(
    @PathVariable String email,
    @RequestBody PointHistory history) {

    try {
      // Service 로직에서 학생 조회, Role 검증, 잔액 업데이트 및 내역 기록을 모두 처리
      PointHistory savedHistory = pointHistoryService.recordTransactionByStudentEmail(email, history);

      return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory);
    } catch (NoSuchElementException | NotFoundException e) {
      // 학생을 찾지 못한 경우
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IllegalArgumentException e) {
      // 잔액 부족, STUDENT 역할 아님 등의 비즈니스 로직 오류
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
  }
}