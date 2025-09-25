package com.spring.schoolmate.controller;

import com.spring.schoolmate.entity.Product;
import com.spring.schoolmate.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  /**
   * 상품 등록 (ADMIN 권한 필요)
   * @param product 등록할 상품 정보
   * @return 등록된 상품 객체
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public ResponseEntity<Product> registerProduct(@RequestBody Product product) {
    Product registeredProduct = productService.registerProduct(product);
    return new ResponseEntity<>(registeredProduct, HttpStatus.CREATED);
  }

  /**
   * 상품 정보 수정 (ADMIN 권한 필요)
   * @param productId 수정할 상품 ID
   * @param updatedProduct 새로운 상품 정보
   * @return 수정된 상품 객체
   */
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{productId}")
  public ResponseEntity<Product> updateProduct(@PathVariable Integer productId, @RequestBody Product updatedProduct) {
    try {
      Product product = productService.updateProduct(productId, updatedProduct);
      return ResponseEntity.ok(product);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
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