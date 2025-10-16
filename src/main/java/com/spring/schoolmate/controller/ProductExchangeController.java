package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.ProductExchange;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.ProductExchangeService;
import io.swagger.v3.oas.annotations.Operation; // Operation 어노테이션 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

/**
 * 상품 교환 및 관리 관련 컨트롤러.
 * 포인트 상품 교환 요청, 사용 완료 처리, 교환 내역 조회 기능을 제공합니다.
 */
@Tag(name = "Product Exchange", description = "포인트 상품 교환 및 내역 관리 API")
@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
@Slf4j // 로깅 사용을 위해 추가
public class ProductExchangeController {

  private final ProductExchangeService productExchangeService;

  /**
   * 상품 교환 요청 (STUDENT 권한 필요)
   * @param productId 교환할 상품 ID
   * @param authentication 현재 로그인한 사용자 정보 (Student ID 획득용)
   * @return 교환 완료된 ProductExchange 객체
   */
  @Operation(
    summary = "상품 교환 요청 (포인트 사용)",
    description = "로그인된 학생이 특정 상품 ID에 대해 포인트를 사용하고 교환을 요청합니다. 포인트가 차감되며, 재고 및 포인트 부족 시 오류를 반환합니다. [권한: STUDENT]"
  )
  @PreAuthorize("hasAuthority('STUDENT')")
  @PostMapping("/{productId}")
  public ResponseEntity<?> exchangeProduct(
    @PathVariable Integer productId,
    Authentication authentication) {

    // studentId는 Long 타입입니다.
    CustomStudentDetails userDetails = (CustomStudentDetails) authentication.getPrincipal();
    Long studentId = userDetails.getStudent().getStudentId();

    try {
      ProductExchange newExchange = productExchangeService.exchangeProduct(studentId, productId);
      return new ResponseEntity<>(newExchange, HttpStatus.CREATED);
    } catch (NoSuchElementException e) {
      // 학생 또는 상품 미발견 시 404 NOT_FOUND
      log.warn("상품 교환 실패: 학생 또는 상품을 찾을 수 없습니다. Student ID: {}, Product ID: {}", studentId, productId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      // 포인트 부족 또는 재고 부족 시 400 BAD_REQUEST
      log.warn("상품 교환 실패: 포인트 또는 재고 부족. Student ID: {}, Product ID: {}", studentId, productId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      log.error("상품 교환 중 예상치 못한 오류 발생. Student ID: {}, Product ID: {}", studentId, productId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 교환 중 예상치 못한 오류가 발생했습니다.");
    }
  }

  // ---

  /**
   * 교환 상품 사용 상태를 '사용완료'로 변경 (STUDENT 권한 필요)
   * @param productExchangeId 상태를 변경할 ProductExchange의 ID
   * @return 업데이트된 ProductExchange 객체
   */
  @Operation(
    summary = "교환 상품 사용 완료 처리",
    description = "학생이 교환한 상품(쿠폰 등)을 사용한 후, 해당 교환 내역 ID의 상태를 '사용 완료'로 변경합니다. [권한: STUDENT]"
  )
  @PreAuthorize("hasAuthority('STUDENT')")
  @PutMapping("/{productExchangeId}/use")
  public ResponseEntity<?> useProduct(@PathVariable Integer productExchangeId) {
    try {
      ProductExchange usedExchange = productExchangeService.useProduct(productExchangeId);
      return ResponseEntity.ok(usedExchange);
    } catch (NoSuchElementException e) {
      // NotFoundException 대신 NoSuchElementException을 사용하도록 Service에서 변경했으므로 수정
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  // ---

  /**
   * 로그인한 학생의 교환 상품 목록을 페이지네이션하여 조회 (STUDENT 권한 필요)
   * @param pageable 페이지네이션 정보
   * @param authentication 현재 로그인한 사용자 정보 (Student ID 획득용)
   * @return 페이징 처리된 교환 상품 목록
   */
  @Operation(
    summary = "본인(학생)의 상품 교환 내역 조회 (페이지네이션)",
    description = "로그인된 학생이 과거에 교환한 상품 목록을 최신순으로 페이지네이션하여 조회합니다. [권한: STUDENT]"
  )
  @PreAuthorize("hasAuthority('STUDENT')")
  @GetMapping("/my-exchanges")
  public ResponseEntity<Page<ProductExchange>> getMyExchangedProducts(
    @PageableDefault(size = 10, sort = "exchangeDate") Pageable pageable,
    Authentication authentication) {

    CustomStudentDetails userDetails = (CustomStudentDetails) authentication.getPrincipal();
    Long studentId = userDetails.getStudent().getStudentId();

    // 🚨 오류 수정: studentId.intValue() 대신 Long 타입인 studentId를 그대로 전달
    Page<ProductExchange> exchanges = productExchangeService.getExchangedProductsByStudentId(studentId, pageable);
    return ResponseEntity.ok(exchanges);
  }
}