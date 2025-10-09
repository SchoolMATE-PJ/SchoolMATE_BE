package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.entity.ProductExchange;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.exception.NotFoundException; // 사용자 정의 예외
import com.spring.schoolmate.repository.ProductExchangeRepository;
import com.spring.schoolmate.repository.ProductRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // LocalDate 대신 LocalDateTime 사용
import java.util.Date;
import java.util.NoSuchElementException; // 기존 사용 예외는 유지
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductExchangeService {

  private final ProductExchangeRepository productExchangeRepository;
  private final StudentRepository studentRepository;
  private final ProductRepository productRepository;
  private final PointHistoryService pointHistoryService;

  /**
   * 상품 교환 기능 (포인트 차감, 재고 감소, 교환 내역 기록)
   *
   * @param studentId 교환을 요청한 학생의 ID
   * @param productId 교환할 상품의 ID
   * @return 저장된 ProductExchange 객체
   */
  @Transactional
  public ProductExchange exchangeProduct(Long studentId, Integer productId) {
    // 1. 학생 및 상품 존재 여부 확인
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NotFoundException("ID " + studentId + "에 해당하는 학생을 찾을 수 없습니다."));

    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new NotFoundException("ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

    // 2. 재고 확인
    if (product.getStock() <= 0) {
      throw new IllegalArgumentException(product.getProductName() + " 상품의 재고가 부족합니다.");
    }

    // 3. 사용 가능 포인트 확인
    int requiredPoints = product.getProductPoints();
    if (student.getPointBalance() < requiredPoints) {
      throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다. 현재 포인트: " + student.getPointBalance() + ", 필요한 포인트: " + requiredPoints);
    }

    // 4. 상품 교환 로직 실행

    // 4.1. 학생 포인트 차감 및 PointHistory 기록
    pointHistoryService.recordTransaction(
      studentId,
      -requiredPoints, // 차감은 음수
      product.getProductId().longValue(), // refId: 상품 ID
      "PRODUCT", // refType: 상품
      "EXCHANGE" // transactionType: 교환
    );

    // 4.2. 상품 재고 감소
    product.setStock(product.getStock() - 1);
    // 상품 엔티티는 재고가 변경되었으므로 저장합니다.
    productRepository.save(product);

    // 4.3. ProductExchange 엔티티 생성 및 저장
    ProductExchange newExchange = new ProductExchange();

    // ⭐️ LocalDateTime.now()를 사용하여 현재 교환 시점을 설정 ⭐️
    LocalDateTime exchangeDate = LocalDateTime.now();

    // ⭐️ 만료일 계산: exchangeDate로부터 12개월 후 ⭐️
    LocalDateTime expirationDate = exchangeDate.plusYears(1);

    newExchange.setStudent(student);
    newExchange.setProduct(product);
    newExchange.setExchangeDate(exchangeDate);       // ⭐️ 수정: exchangeDate 설정 ⭐️
    newExchange.setExpirationDate(expirationDate);   // ⭐️ 수정: 만료일 설정 (NOT NULL 오류 해결) ⭐️
    newExchange.setExchangeCardStatus("미사용");

    return productExchangeRepository.save(newExchange);
  }

  /**
   * 교환 상품 사용 상태를 '사용완료'로 변경
   * @param productExchangeId 상태를 변경할 ProductExchange의 ID
   * @return 업데이트된 ProductExchange 객체
   */
  @Transactional
  public ProductExchange useProduct(Integer productExchangeId) {
    // NoSuchElementException 대신 NotFoundException을 사용하는 것이 일관성 있습니다.
    ProductExchange exchange = productExchangeRepository.findById(productExchangeId)
      .orElseThrow(() -> new NotFoundException("ID " + productExchangeId + "에 해당하는 교환 상품을 찾을 수 없습니다."));

    exchange.setUsageDate(new Date()); // 사용일자 업데이트
    exchange.setExchangeCardStatus("사용완료"); // 상태 변경
    return productExchangeRepository.save(exchange);
  }

  /**
   * 학생 ID로 교환 상품 목록을 페이지네이션하여 조회
   * @param studentId 학생 ID
   * @param pageable 페이지네이션 정보 (페이지 번호, 크기 등)
   * @return 페이징 처리된 교환 상품 목록
   */
  public Page<ProductExchange> getExchangedProductsByStudentId(Long studentId, Pageable pageable) {
    // Long 타입의 studentId를 바로 사용합니다.
    return productExchangeRepository.findByStudent_StudentIdOrderByExchangeDateDesc(studentId, pageable);
  }
}