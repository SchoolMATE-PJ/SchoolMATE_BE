package com.spring.schoolmate.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReq {

  private String productName;
  private Integer productPoints;
  private LocalDate expirationDate;
  private Integer stock;
  private Integer totalQuantity;
}