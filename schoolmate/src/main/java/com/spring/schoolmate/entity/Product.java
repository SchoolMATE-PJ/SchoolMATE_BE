package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
  private LocalDate expirationDate;

  @Column(nullable = false)
  private Integer stock;

  @Column(nullable = false)
  private Integer totalQuantity;

  @Column(length = 255)
  private String imageUrl;

  @CreationTimestamp
  private LocalDateTime registrationDate;
}