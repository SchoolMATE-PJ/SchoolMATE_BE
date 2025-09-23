package com.spring.schoolmate.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReq {

  private String productName;
  private Integer productPoints;
  private Date expirationDate;
  private Integer stock;
  private Integer totalQuantity;
}