package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.entity.ProductExchange;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.ProductExchangeRepository;
import com.spring.schoolmate.repository.ProductRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor; // 생성자 주입을 위해 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // jakarta.transaction.Transactional 대신 Spring의 트랜잭션 사용

import java.util.Date;
import java.util.NoSuchElementException; // NoSuchElementException 추가
import java.util.Optional;
import com.spring.schoolmate.service.PointHistoryService;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
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
  @Transactional // 쓰기 작업이 필요하므로 @Transactional 명시
  public ProductExchange exchangeProduct(Long studentId, Integer productId) {
    // 1. 학생 및 상품 존재 여부 확인
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("ID " + studentId + "에 해당하는 학생을 찾을 수 없습니다."));

    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new NoSuchElementException("ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

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

    // 4.1. 학생 포인트 차감 및 PointHistory 기록 (⭐️ 핵심 수정 ⭐️)

    // student.setPointBalance(student.getPointBalance() - requiredPoints); // ❌ 직접 차감 로직 제거
    // studentRepository.save(student); // ❌ 직접 저장 로직 제거 (PointHistoryService 내에서 처리됨)

    // PointHistoryService를 통해 포인트 차감 및 기록
    pointHistoryService.recordTransaction(
      studentId,
      -requiredPoints, // 차감은 음수
      product.getProductId().longValue(), // refId: 상품 ID
      "PRODUCT", // refType: 상품
      "EXCHANGE" // transactionType: 교환
    );

    // 4.2. 상품 재고 감소
    product.setStock(product.getStock() - 1);
    productRepository.save(product); // Product 엔티티 업데이트 (재고 변경 사항 커밋)

    // 4.3. ProductExchange 엔티티 생성 및 저장
    ProductExchange newExchange = new ProductExchange();
    // ... (ProductExchange 필드 설정 로직 유지)
    newExchange.setStudent(student);
    newExchange.setProduct(product);
    newExchange.setExchangeDate(new Date());
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
    Optional<ProductExchange> optional = productExchangeRepository.findById(productExchangeId);
    if (optional.isPresent()) {
      ProductExchange exchange = optional.get();
      exchange.setUsageDate(new Date()); // 사용일자 업데이트
      exchange.setExchangeCardStatus("사용완료"); // 상태 변경
      return productExchangeRepository.save(exchange);
    } else {
      throw new NoSuchElementException("ID " + productExchangeId + "에 해당하는 교환 상품을 찾을 수 없습니다.");
    }
  }

  /**
   * 학생 ID로 교환 상품 목록을 페이지네이션하여 조회
   * @param studentId 학생 ID
   * @param pageable 페이지네이션 정보 (페이지 번호, 크기 등)
   * @return 페이징 처리된 교환 상품 목록
   */
  public Page<ProductExchange> getExchangedProductsByStudentId(Long studentId, Pageable pageable) {
    // 💡 repository 메서드의 첫 번째 인자가 Long 타입이어야 하므로, Integer 대신 Long으로 변경
    return productExchangeRepository.findByStudent_StudentIdOrderByExchangeDateDesc(studentId, pageable);
  }
}