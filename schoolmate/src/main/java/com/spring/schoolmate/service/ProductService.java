package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  /**
   * 상품 등록
   * @return 등록된 상품 객체
   */
  @Transactional
  public Product registerProduct(Product product) {
    // 비즈니스 로직 추가 가능 (예: 상품 코드 중복 확인 등)
    String productName = product.getProductName();
    String prefix = "";
    String category = "";

    // 상품명에 따라 상품 코드의 접두사와 카테고리 설정
    if (productName.contains("카페") || productName.contains("라떼") || productName.contains("아메리카노")
    || productName.contains("프라페")) {
      prefix = "CO";
      category = "커피";
    } else if (productName.contains("CU") || productName.contains("세븐일레븐") || productName.contains("GS25")) {
      prefix = "CS";
      category = "편의점";
    } else if (productName.contains("배달의 민족") || productName.contains("쿠팡이츠") || productName.contains("요기요")) {
      prefix = "BM";
      category = "배달음식";
    } else if (productName.contains("CGV") || productName.contains("롯데시네마") || productName.contains("메가박스")) {
      prefix = "MO";
      category = "영화";
    }

    // 카테고리 설정(필요한 경우)
    if (!category.isEmpty()) {
      product.setProductCategory(category);
    }

    // 중복되지 않는 상품 코드 생섬 및 설정
    if (!prefix.isEmpty()) {
      String newProductCode;
      Random random = new Random();
      do {
        int randomNumber = random.nextInt(900) + 100; // 100부터 999까지의 랜덤 숫자
        newProductCode = prefix + randomNumber;
      } while (productRepository.findByProductCode(newProductCode) != null);
      product.setProductCode(newProductCode);
    }

    return productRepository.save(product);
  }

  /**
   * 상품 정보 수정
   * @return 수정된 상품 객체
   */
  @Transactional
  public Product updateProduct(Integer productId, Product updatedProduct) {
    Optional<Product> optionalProduct = productRepository.findById(productId);
    if (optionalProduct.isPresent()) {
      Product existingProduct = optionalProduct.get();
      existingProduct.setProductName(updatedProduct.getProductName());
      existingProduct.setProductPoints(updatedProduct.getProductPoints());
      existingProduct.setExpirationDate(updatedProduct.getExpirationDate());

      return productRepository.save(existingProduct);
    } else {
      throw new NotFoundException("상품을 찾을 수 없습니다: " + productId);
    }
  }

  /**
   * 특정 상품 ID로 상품 조회
   * @return 조회된 상품 객체 (존재하지 않으면 Optional.empty)
   */
  @Transactional
  public Optional<Product> getProductById(Integer productId) {
    return productRepository.findById(productId);
  }

  /**
   * 상품 전체 목록 조회
   * @return 상품 목록
   */
  @Transactional
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  /**
   * 상품명으로 상품 목록 조회
   * @return 해당 상품명의 상품 목록
   */
  @Transactional
  public List<Product> getProductsByName(String productName) {
    return productRepository.findByProductName(productName);
  }

  /**
   * 상품 코드로 상품 조회
   * @return 해당 상품 코드의 상품 객체
   */
  @Transactional
  public Product getProductByCode(String productCode) {
    return productRepository.findByProductCode(productCode);
  }

  /**
   * 상품 카테고리로 상품 목록 조회
   * @return 해당 카테고리의 상품 목록
   */
  @Transactional
  public List<Product> getProductsByCategory(String productCategory) {
    return productRepository.findByProductCategory(productCategory);
  }

  /**
   * 특정 상품 ID의 상품 삭제
   */
  @Transactional
  public void deleteProduct(Integer productId) {
    productRepository.deleteById(productId);
  }

  /**
   * 재고가 0인 상품들을 삭제
   * @return 삭제된 상품의 수
   */
  @Transactional
  public int deleteOutOfStockProducts() {
    return productRepository.deleteByStockIsZeroNative();
  }
}