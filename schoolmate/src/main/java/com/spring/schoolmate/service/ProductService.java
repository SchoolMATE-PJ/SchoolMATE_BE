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
    String productName = product.getProductName();
    String prefix = "";
    String category = "";

    // 1. 상품명에 따라 상품 코드의 접두사와 카테고리 설정
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

    // 🚨 [수정]: 키워드가 없을 경우 'ETC'로 설정하여 상품 코드 생성을 보장
    if (prefix.isEmpty()) {
      prefix = "ETC";
      category = "기타";
    }

    // 2. 카테고리 설정 (이제 category는 비어있지 않음)
    product.setProductCategory(category);

    // 3. 중복되지 않는 상품 코드 생성 및 설정 (prefix는 항상 존재함)
    String newProductCode;
    Random random = new Random();
    do {
      int randomNumber = random.nextInt(900) + 100; // 100부터 999까지의 랜덤 숫자
      newProductCode = prefix + randomNumber;
    } while (productRepository.findByProductCode(newProductCode) != null);

    product.setProductCode(newProductCode);

    // 이 시점에는 productCode와 productCategory가 항상 설정됩니다.
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

      // 🚨 [개선]: DTO를 통해 받은 필드만 업데이트하거나, 널 체크 로직을 추가하는 것이 더 안전함
      // 현재는 엔티티를 통째로 받았다고 가정하고, 명시된 필드만 업데이트.
      existingProduct.setProductName(updatedProduct.getProductName());
      existingProduct.setProductPoints(updatedProduct.getProductPoints());
      existingProduct.setExpirationDate(updatedProduct.getExpirationDate());

      // 만약 재고(stock)나 총수량(totalQuantity) 필드가 엔티티에 있다면 DTO와 맞춰서 추가.
      // existingProduct.setStock(updatedProduct.getStock());
      // existingProduct.setTotalQuantity(updatedProduct.getTotalQuantity());

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