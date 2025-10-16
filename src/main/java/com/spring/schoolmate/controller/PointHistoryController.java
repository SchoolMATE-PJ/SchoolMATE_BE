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
import java.util.stream.Collectors;

/**
 * 포인트 거래 내역 및 잔액 조회 컨트롤러.
 * 학생 본인의 포인트 잔액, 거래 내역 조회 및 포인트 지급/차감 기능을 제공합니다.
 */
@Tag(name = "Point History", description = "학생 포인트 잔액 조회 및 거래 내역 관리 API")
@RestController
@RequestMapping("/api/point-history")
@RequiredArgsConstructor
public class PointHistoryController {

  private final PointHistoryService pointHistoryService;
  private final StudentService studentService;

  //-------------------------------------------------------------------------
  // 학생 본인 조회 엔드포인트
  //-------------------------------------------------------------------------

  /**
   * 로그인된 학생의 현재 보유 포인트를 조회. (authToken 기반)
   * GET /api/point-history/student/me/balance
   * @param authentication Spring Security의 인증 정보
   * @return 현재 보유 포인트 (Integer)
   */
  @Operation(
    summary = "본인(학생)의 현재 포인트 잔액 조회",
    description = "로그인된 학생의 현재 보유하고 있는 총 포인트를 조회합니다."
  )
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
   * 로그인된 학생의 모든 포인트 거래 내역을 최신순으로 조회. (authToken 기반)
   * GET /api/point-history/student/me
   * @param authentication Spring Security의 인증 정보
   * @return PointHistoryRes DTO 목록 (최신순)
   */
  @Operation(
    summary = "본인(학생)의 전체 포인트 거래 내역 조회",
    description = "로그인된 학생의 모든 포인트 지급 및 사용 내역을 최신순으로 조회합니다."
  )
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
  @Operation(
    summary = "본인(학생)의 급식 사진 업로드 횟수 조회",
    description = "로그인된 학생이 포인트를 지급받은 급식 사진 인증 횟수를 조회합니다."
  )
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
  @Operation(
    summary = "본인(학생)의 총 사용 포인트 합계 조회",
    description = "로그인된 학생이 상품 구매 등으로 인해 사용(차감)한 포인트의 총합을 조회합니다."
  )
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

  //-------------------------------------------------------------------------
  // 관리자/내부 시스템용 (이메일 기반)
  //-------------------------------------------------------------------------

  /**
   * 특정 학생의 이메일을 기반으로 모든 포인트 거래 내역을 최신순으로 조회.
   * @param email 조회할 학생의 이메일
   * @return PointHistoryRes DTO 목록 (최신순)
   */
  @Operation(
    summary = "특정 학생의 포인트 거래 내역 조회 (관리자용)",
    description = "관리자가 특정 학생의 이메일을 사용하여 해당 학생의 모든 포인트 거래 내역을 조회합니다."
  )
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
   * @param email   거래를 기록할 학생의 이메일
   * @param history 기록할 PointHistory 객체 (amount, tsType 등이 포함되어야 함. JSON Body)
   * @return 저장된 PointHistory 객체의 DTO와 201 Created 응답
   */
  @Operation(
    summary = "특정 학생에게 포인트 지급/차감 기록 (관리자/시스템용)",
    description = "관리자 또는 시스템 내부에서 특정 이메일 학생의 포인트 거래(지급/차감)를 기록합니다."
  )
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
}