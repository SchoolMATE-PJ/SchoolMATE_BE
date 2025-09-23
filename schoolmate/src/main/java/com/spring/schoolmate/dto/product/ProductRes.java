package com.spring.schoolmate.dto;

import com.spring.schoolmate.entity.Product;
import lombok.*;

import java.util.Date;

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
  private Date registrationDate;

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