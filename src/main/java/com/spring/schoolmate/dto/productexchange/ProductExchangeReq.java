package com.spring.schoolmate.dto.productexchange;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductExchangeReq {
  private Integer studentId;
  private Integer productId;
}