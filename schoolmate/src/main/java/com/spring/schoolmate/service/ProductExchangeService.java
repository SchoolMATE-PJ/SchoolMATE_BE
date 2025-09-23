package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.entity.ProductExchange;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.ProductExchangeRepository;
import com.spring.schoolmate.repository.ProductRepository;
import com.spring.schoolmate.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ProductExchangeService {

  private final ProductExchangeRepository productExchangeRepository;
  private final StudentRepository studentRepository;
  private final ProductRepository productRepository;

  public ProductExchangeService(ProductExchangeRepository productExchangeRepository,
                                StudentRepository studentRepository,
                                ProductRepository productRepository) {
    this.productExchangeRepository = productExchangeRepository;
    this.studentRepository = studentRepository;
    this.productRepository = productRepository;
  }

  /**
   * 상품 교환 기능 (상품 교환일, 사용 상태 초기화)
   * @param studentId 교환을 요청한 학생의 ID
   * @param productId 교환할 상품의 ID
   * @return 저장된 ProductExchange 객체
   */
  @Transactional
  public ProductExchange exchangeProduct(Long studentId, Integer productId) {
    Optional<Student> studentOptional = studentRepository.findById(studentId);
    Optional<Product> productOptional = productRepository.findById(productId);

    if (studentOptional.isPresent() && productOptional.isPresent()) {
      Student student = studentOptional.get();
      Product product = productOptional.get();

      // 사용 가능 포인트 확인 및 차감 로직
      if (student.getPointBalance() >= product.getProductPoints()) {
        student.setPointBalance(student.getPointBalance() - product.getProductPoints());
        studentRepository.save(student);

        // ProductExchange 엔티티 생성 및 저장
        ProductExchange newExchange = new ProductExchange();
        newExchange.setStudent(student);
        newExchange.setProduct(product);
        newExchange.setExchangeDate(new Date()); // 현재 날짜로 교환일 설정
        newExchange.setExchangeCardStatus("미사용"); // 교환 후 초기 상태는 '미사용'

        return productExchangeRepository.save(newExchange);
      } else {
        throw new NotFoundException("사용 가능한 포인트가 부족합니다.");
      }
    } else {
      throw new NotFoundException("해당 학생 또는 상품을 찾을 수 없습니다.");
    }
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
      throw new NotFoundException("해당 교환 상품을 찾을 수 없습니다.");
    }
  }

  /**
   * 학생 ID로 교환 상품 목록을 페이지네이션하여 조회
   * @param studentId 학생 ID
   * @param pageable 페이지네이션 정보 (페이지 번호, 크기 등)
   * @return 페이징 처리된 교환 상품 목록
   */
  public Page<ProductExchange> getExchangedProductsByStudentId(Integer studentId, Pageable pageable) {
    return productExchangeRepository.findByStudent_StudentIdOrderByExchangeDateDesc(studentId, pageable);
  }
}