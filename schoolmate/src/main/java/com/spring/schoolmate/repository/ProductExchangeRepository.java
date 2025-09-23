package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.ProductExchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductExchangeRepository extends JpaRepository<ProductExchange, Integer> {

  // 학생 ID로 교환 상품 목록을 페이지네이션하여 조회
  Page<ProductExchange> findByStudent_StudentIdOrderByExchangeDateDesc(Integer studentId, Pageable pageable);

  // 사용 상태별로 교환 상품을 페이지네이션하여 조회 (예: '사용중', '사용완료')
  Page<ProductExchange> findByStudent_StudentIdAndExchangeCardStatusOrderByExchangeDateDesc(
    Integer studentId, String status, Pageable pageable);
}