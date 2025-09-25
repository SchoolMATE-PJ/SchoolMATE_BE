package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.ProductExchange;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.ProductExchangeService;
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

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ProductExchangeController {

  private final ProductExchangeService productExchangeService;

  /**
   * 상품 교환 요청 (STUDENT 권한 필요)
   * @param productId 교환할 상품 ID
   * @param authentication 현재 로그인한 사용자 정보 (Student ID 획득용)
   * @return 교환 완료된 ProductExchange 객체
   */
  @PreAuthorize("hasAuthority('STUDENT')")
  @PostMapping("/{productId}")
  public ResponseEntity<?> exchangeProduct(
    @PathVariable Integer productId,
    Authentication authentication) {

    CustomStudentDetails userDetails = (CustomStudentDetails) authentication.getPrincipal();
    Long studentId = userDetails.getStudent().getStudentId();

    try {
      ProductExchange newExchange = productExchangeService.exchangeProduct(studentId, productId);
      return new ResponseEntity<>(newExchange, HttpStatus.CREATED);
    } catch (NotFoundException e) {
      // 포인트 부족 또는 학생/상품 미발견 시 400 Bad Request 또는 404 Not Found
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 교환 중 오류가 발생했습니다.");
    }
  }

  /**
   * 교환 상품 사용 상태를 '사용완료'로 변경 (STUDENT 권한 필요)
   * @param productExchangeId 상태를 변경할 ProductExchange의 ID
   * @return 업데이트된 ProductExchange 객체
   */
  @PreAuthorize("hasAuthority('STUDENT')")
  @PutMapping("/{productExchangeId}/use")
  public ResponseEntity<?> useProduct(@PathVariable Integer productExchangeId) {
    try {
      ProductExchange usedExchange = productExchangeService.useProduct(productExchangeId);
      return ResponseEntity.ok(usedExchange);
    } catch (NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  /**
   * 로그인한 학생의 교환 상품 목록을 페이지네이션하여 조회 (STUDENT 권한 필요)
   * @param pageable 페이지네이션 정보
   * @param authentication 현재 로그인한 사용자 정보 (Student ID 획득용)
   * @return 페이징 처리된 교환 상품 목록
   */
  @PreAuthorize("hasAuthority('STUDENT')")
  @GetMapping("/my-exchanges")
  public ResponseEntity<Page<ProductExchange>> getMyExchangedProducts(
    @PageableDefault(size = 10, sort = "exchangeDate") Pageable pageable,
    Authentication authentication) {

    CustomStudentDetails userDetails = (CustomStudentDetails) authentication.getPrincipal();
    Long studentId = userDetails.getStudent().getStudentId();

    Page<ProductExchange> exchanges = productExchangeService.getExchangedProductsByStudentId(studentId.intValue(), pageable);
    return ResponseEntity.ok(exchanges);
  }
}