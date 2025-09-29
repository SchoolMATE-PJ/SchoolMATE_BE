package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.time.LocalDateTime; // 🚨 [추가] LocalDateTime 임포트

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
  private Integer productId;

  @Column(length = 5, nullable = false, unique = true)
  private String productCode;

  @Column(length = 10, nullable = false)
  private String productCategory;

  @Column(length = 100, nullable = false)
  private String productName;

  @Column(nullable = false)
  private Integer productPoints;

  @Column(nullable = false)
  private Date expirationDate;

  @Column(nullable = false)
  private Integer stock;

  @Column(nullable = false)
  private Integer totalQuantity;

  @CreationTimestamp
  // 🚨 [수정] DB NOT NULL 오류 해결 및 최신 표준 적용
  private LocalDateTime registrationDate;
}