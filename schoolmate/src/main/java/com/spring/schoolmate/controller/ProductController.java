package com.spring.schoolmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.schoolmate.dto.product.ProductReq;
import com.spring.schoolmate.dto.product.ProductRes;
import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final ObjectMapper objectMapper;

  // 상품 등록 (POST)
  @PostMapping(consumes = {"multipart/form-data"})
  public ResponseEntity<ProductRes> registerProduct(
    @RequestPart("product") String productJson,
    @RequestPart(value = "file", required = false) MultipartFile file) {
    try {
      // String으로 받은 product JSON을 DTO로 변환
      ProductReq productReq = objectMapper.readValue(productJson, ProductReq.class);

      // ProductReq를 Product Entity로 변환 (변환 로직 필요)
      Product newProduct = convertToEntity(productReq);

      // Service를 호출하여 이미지와 함께 등록
      Product registeredProduct = productService.registerProduct(newProduct, file);

      return ResponseEntity.status(HttpStatus.CREATED).body(ProductRes.fromEntity(registeredProduct));

    } catch (IOException e) {
      // JSON 파싱 오류 또는 파일 I/O 오류 처리
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  // 상품 수정 (PUT)
  @PutMapping(value = "/{productId}", consumes = {"multipart/form-data"})
  public ResponseEntity<ProductRes> updateProduct(
    @PathVariable Integer productId,
    @RequestPart("product") String productJson,
    @RequestPart(value = "file", required = false) MultipartFile file) {
    try {
      ProductReq productReq = objectMapper.readValue(productJson, ProductReq.class);
      Product updatedProduct = convertToEntity(productReq);

      Product result = productService.updateProduct(productId, updatedProduct, file);

      return ResponseEntity.ok(ProductRes.fromEntity(result));

    } catch (NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  private Product convertToEntity(ProductReq productReq) {
    return Product.builder()
      .productName(productReq.getProductName())
      .productPoints(productReq.getProductPoints())
      .expirationDate(productReq.getExpirationDate())
      .stock(productReq.getStock())
      .totalQuantity(productReq.getTotalQuantity())
      .build();
  }


  /**
   * 특정 상품 ID로 상품 조회 (STUDENT, ADMIN 권한 허용)
   * @param productId 조회할 상품 ID
   * @return 조회된 상품 객체
   */
  @PreAuthorize("hasAnyAuthority('STUDENT', 'ADMIN')")
  @GetMapping("/{productId}")
  public ResponseEntity<Product> getProductById(@PathVariable Integer productId) {
    Optional<Product> product = productService.getProductById(productId);
    return product.map(ResponseEntity::ok)
      .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  /**
   * 상품 전체 목록 조회 (STUDENT, ADMIN 권한 허용)
   * @return 상품 목록
   */
  @PreAuthorize("hasAnyAuthority('STUDENT', 'ADMIN')")
  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  /**
   * 상품명으로 상품 목록 조회 (STUDENT, ADMIN 권한 허용)
   * @param productName 조회할 상품명
   * @return 해당 상품명의 상품 목록
   */
  @PreAuthorize("hasAnyAuthority('STUDENT', 'ADMIN')")
  @GetMapping("/search/name")
  public ResponseEntity<List<Product>> getProductsByName(@RequestParam String productName) {
    List<Product> products = productService.getProductsByName(productName);
    return ResponseEntity.ok(products);
  }

  /**
   * 상품 카테고리로 상품 목록 조회 (STUDENT, ADMIN 권한 허용)
   * @param productCategory 조회할 카테고리
   * @return 해당 카테고리의 상품 목록
   */
  @PreAuthorize("hasAnyAuthority('STUDENT', 'ADMIN')")
  @GetMapping("/search/category")
  public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam String productCategory) {
    List<Product> products = productService.getProductsByCategory(productCategory);
    return ResponseEntity.ok(products);
  }

  /**
   * 특정 상품 ID의 상품 삭제 (ADMIN 권한 필요)
   * @param productId 삭제할 상품 ID
   * @return 응답
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
    try {
      productService.deleteProduct(productId);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}