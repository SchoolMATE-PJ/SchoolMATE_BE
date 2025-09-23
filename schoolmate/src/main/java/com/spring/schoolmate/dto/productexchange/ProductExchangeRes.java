package com.spring.schoolmate.dto;

import com.spring.schoolmate.entity.ProductExchange;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class ProductExchangeRes {
  private Integer productExchangeId;
  private String studentName; // 학생 이름
  private String productName; // 상품명
  private Integer productPoints; // 상품 포인트
  private Date exchangeDate; // 교환 일자
  private String exchangeCardStatus; // 교환 상태

  public static ProductExchangeRes fromEntity(ProductExchange productExchange) {
    return ProductExchangeRes.builder()
      .productExchangeId(productExchange.getProductExchangeId())
      .studentName(productExchange.getStudent().getName())
      .productName(productExchange.getProduct().getProductName())
      .productPoints(productExchange.getProduct().getProductPoints())
      .exchangeDate(productExchange.getExchangeDate())
      .exchangeCardStatus(productExchange.getExchangeCardStatus())
      .build();
  }
}