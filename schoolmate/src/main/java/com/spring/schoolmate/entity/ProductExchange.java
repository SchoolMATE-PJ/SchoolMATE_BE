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
@Table(name = "product_exchange")
public class ProductExchange {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "productexchange_id")
  private Integer productExchangeId; // 교환 상품 아이디

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student; // 학생 엔터티

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product; // 상품 엔터티

  @Column(name = "exchange_date", nullable = false)
  private Date exchangeDate; // 상품 교환 일자

  @Column(name = "usage_date", nullable = true)
  private Date usageDate; // 상품 사용 일자

  @Column(name = "exchangecard_status", length = 20, nullable = true)
  private String exchangeCardStatus; // 교환 상품 사용 상태
}