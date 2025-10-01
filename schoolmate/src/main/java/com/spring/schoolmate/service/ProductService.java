package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 사용됨

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID; // 사용됨

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final FirebaseStorageService firebaseStorageService;

  // 🚨 [추가] 상품명에 따라 상품 코드 접두사 및 카테고리 설정 로직 구현
  private String[] determineCategoryAndPrefix(String productName) {
    String prefix = "ETC";
    String category = "기타";
    String name = productName.toUpperCase();

    if (name.contains("카페") || name.contains("라떼") || name.contains("아메리카노")
      || name.contains("프라페")) {
      prefix = "CO";
      category = "커피";
    } else if (name.contains("CU") || name.contains("세븐일레븐") || name.contains("GS25") || name.contains("상품권")) {
      prefix = "CS";
      category = "편의점";
    } else if (name.contains("배달의 민족") || name.contains("쿠팡이츠") || name.contains("요기요")) {
      prefix = "BE"; // 프론트와 통일
      category = "배달음식";
    } else if (name.contains("CGV") || name.contains("롯데시네마") || name.contains("메가박스") || name.contains("영화")) {
      prefix = "MO";
      category = "영화";
    }
    return new String[]{prefix, category};
  }

  /**
   * 상품 등록 (이미지 파일 포함)
   * @return 등록된 상품 객체
   */
  @Transactional
  public Product registerProduct(Product product, MultipartFile file) {
    // 상품 코드 및 카테고리 설정 로직 (기존 유지)
    String productName = product.getProductName();
    String[] categoryInfo = determineCategoryAndPrefix(productName);
    String prefix = categoryInfo[0];
    String category = categoryInfo[1];
    product.setProductCategory(category);

    String newProductCode;
    Random random = new Random();
    do {
      int randomNumber = random.nextInt(900) + 100;
      newProductCode = prefix + randomNumber;
    } while (productRepository.findByProductCode(newProductCode) != null);
    product.setProductCode(newProductCode);

    // [핵심] 이미지 처리 로직: Firebase Storage에 파일 업로드
    if (file != null && !file.isEmpty()) {
      try {
        String imageUrl = firebaseStorageService.uploadFile(file); // Firebase에 업로드 및 URL 획득
        product.setImageUrl(imageUrl); // 획득한 URL을 엔티티에 저장

      } catch (IOException e) {
        // 파일 I/O 오류 (업로드 실패)
        System.err.println("Firebase 파일 업로드 중 오류 발생: " + e.getMessage());
        // 필요에 따라 RuntimeException을 던지거나, 트랜잭션 롤백을 고려
      }
    }

    return productRepository.save(product);
  }

  /**
   * 상품 정보 수정 (이미지 파일 포함)
   * @return 수정된 상품 객체
   */
  @Transactional
  public Product updateProduct(Integer productId, Product updatedProduct, MultipartFile file) {
    Optional<Product> optionalProduct = productRepository.findById(productId);
    if (optionalProduct.isPresent()) {
      Product existingProduct = optionalProduct.get();

      // 1. 상품명 변경 시 카테고리 재설정 및 기본 필드 업데이트 (기존 유지)
      // ... (기존 updateProduct 로직 유지)
      String newProductName = updatedProduct.getProductName();
      existingProduct.setProductName(newProductName);
      String[] categoryInfo = determineCategoryAndPrefix(newProductName);
      existingProduct.setProductCategory(categoryInfo[1]);
      existingProduct.setProductPoints(updatedProduct.getProductPoints());
      existingProduct.setExpirationDate(updatedProduct.getExpirationDate());
      if (updatedProduct.getStock() != null) {
        existingProduct.setStock(updatedProduct.getStock());
      }
      if (updatedProduct.getTotalQuantity() != null) {
        existingProduct.setTotalQuantity(updatedProduct.getTotalQuantity());
      }

      // 2. 이미지 수정 처리 로직
      if (file != null && !file.isEmpty()) {
        try {
          // 2-1. ⭐️ 기존 파일 삭제 (선택 사항: Storage 공간 절약)
          if (existingProduct.getImageUrl() != null) {
            firebaseStorageService.deleteFile(existingProduct.getImageUrl());
          }

          // 2-2. ⭐️ 새 파일 업로드 및 URL 업데이트
          String imageUrl = firebaseStorageService.uploadFile(file);
          existingProduct.setImageUrl(imageUrl);

        } catch (IOException e) {
          System.err.println("Firebase 파일 수정 처리 중 오류 발생: " + e.getMessage());
        }
      }
      // 새 파일이 없으면 기존 imageUrl은 그대로 유지

      return productRepository.save(existingProduct);
    } else {
      throw new NotFoundException("상품을 찾을 수 없습니다: " + productId);
    }
  }

  // 파일 확장자 추출 헬퍼 메서드 (기존 유지)
  private String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
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
    Optional<Product> optionalProduct = productRepository.findById(productId);
    if (optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      // 추가: DB에서 삭제하기 전에 Firebase Storage 파일도 삭제
      if (product.getImageUrl() != null) {
        firebaseStorageService.deleteFile(product.getImageUrl());
      }
      productRepository.deleteById(productId);
    } else {
      throw new NotFoundException("삭제할 상품을 찾을 수 없습니다: " + productId);
    }
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