// /com/spring/schoolmate/dto/productexchange/ProductExchangeRes.java 파일

package com.spring.schoolmate.dto.productexchange;

import com.spring.schoolmate.entity.ProductExchange;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime; // ⭐️ import 변경

@Getter
@Builder
public class ProductExchangeRes {

  private final Integer productExchangeId;
  private final String productName;
  private final String productCode;
  // ⭐️ Date 타입을 LocalDateTime으로 변경 ⭐️
  private final LocalDateTime exchangeDate;
  private final LocalDateTime expirationDate;
  private final String exchangeCardStatus;

  public static ProductExchangeRes from(ProductExchange productExchange) {
    return ProductExchangeRes.builder()
      .productExchangeId(productExchange.getProductExchangeId())
      .productName(productExchange.getProduct().getProductName())
      .productCode(productExchange.getProduct().getProductCode())
      // ⭐️ 오류가 발생한 부분: LocalDateTime 객체를 그대로 사용 ⭐️
      .exchangeDate(productExchange.getExchangeDate())
      .expirationDate(productExchange.getExpirationDate())
      .exchangeCardStatus(productExchange.getExchangeCardStatus())
      .build();
  }
}