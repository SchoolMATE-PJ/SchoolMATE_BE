package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

  // JpaRepository 기본 CRUD 제공
  // save(), findById(), findAll(), deleteById() 등은 별도로 정의하지 않아도 사용 가능

  // --- Query Method (메서드 이름 기반 쿼리) ---
  List<Product> findByProductName(String productName);

  Product findByProductCode(String productCode);

  List<Product> findByProductCategory(String productCategory);

  // --- Native Query (순수 SQL) ---
  // @Modifying과 @Transactional 어노테이션이 추가됨
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM products WHERE stock = 0", nativeQuery = true)
  int deleteByStockIsZeroNative(); // 메서드 이름을 쿼리에 맞게 수정
}