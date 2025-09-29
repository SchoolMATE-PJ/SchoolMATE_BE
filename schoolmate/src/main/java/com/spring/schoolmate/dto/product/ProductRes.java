package com.spring.schoolmate.dto.product;

import com.spring.schoolmate.entity.Product;
import lombok.*;

import java.util.Date;
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
  private Date expirationDate;
  private Integer stock;
  private LocalDateTime registrationDate;

  /**
   * Product 엔티티를 ProductRes DTO로 변환.
   * totalQuantity 필드는 응답 DTO에서 제외됨.
   * @param product 변환할 Product 엔티티
   * @return ProductRes DTO
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
      .build();
  }
}