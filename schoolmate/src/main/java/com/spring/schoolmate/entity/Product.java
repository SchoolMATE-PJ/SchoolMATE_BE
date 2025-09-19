package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Integer productId; // DB의 내부 식별자로 사용되는 고유 번호

  @Column(name = "product_code", length = 50, nullable = false, unique = true)
  private String productCode; // 상품번호 (CO123, CS352 등)

  @Column(name = "product_category", length = 100, nullable = false)
  private String productCategory; // 상품 카테고리

  @Column(name = "product_name", length = 100, nullable = false)
  private String productName; // 상품명

  @Column(name = "product_points", nullable = false)
  private Integer productPoints; // 상품 포인트

  @Column(name = "expiration_date", nullable = false)
  private Date expirationDate; // 유효기간

  @Column(name = "stock", nullable = false)
  private Integer stock; // 재고 수량

  @Column(name = "total_quantity", nullable = false)
  private Integer totalQuantity; // 전체 수량

  @Column(name = "registration_date", nullable = false)
  private Date registrationDate; // 상품 등록 일자
}