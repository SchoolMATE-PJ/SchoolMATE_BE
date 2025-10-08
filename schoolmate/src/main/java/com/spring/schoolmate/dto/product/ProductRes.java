package com.spring.schoolmate.dto.product;

import com.spring.schoolmate.entity.Product;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRes {

  private Integer productId;
  private String productCode;
  private String productCategory;
  private String productName;
  private Integer productPoints;
  private LocalDate expirationDate;
  private Integer stock;
  private LocalDateTime registrationDate;
  private String imageUrl;

  /**
   * Product 엔티티를 ProductRes DTO로 변환.
   */
  public static ProductRes fromEntity(Product product) {
    return ProductRes.builder()
      .productId(product.getProductId())
      .productCode(product.getProductCode())
      .productCategory(product.getProductCategory())
      .productName(product.getProductName())
      .productPoints(product.getProductPoints())
      .expirationDate(product.getExpirationDate())
      .stock(product.getStock())
      .registrationDate(product.getRegistrationDate())
      .imageUrl(product.getImageUrl()) // 🚨 [추가] 이미지 URL 매핑
      .build();
  }
}