package com.spring.schoolmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // 👈 Import 추가
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
  private Integer productExchangeId; // 교환 상품 아이디

  // 1. Student 참조 무시: Student 엔티티의 상세 정보가 직렬화되면서 순환 참조(혹은 깊은 참조) 발생을 방지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  @JsonIgnore // 👈 추가
  private Student student; // 학생 엔터티

  // 2. Product 참조 무시: Product 엔티티의 상세 정보가 직렬화되면서 순환 참조(혹은 깊은 참조) 발생을 방지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore // 👈 추가
  private Product product; // 상품 엔터티

  @Column(nullable = false)
  private Date exchangeDate; // 상품 교환 일자

  @Column(nullable = true)
  private Date usageDate; // 상품 사용 일자

  @Column(length = 20, nullable = true)
  private String exchangeCardStatus; // 교환 상품 사용 상태

  // 학생 ID를 JSON에 포함시키기 위한 메서드
  public Long getStudentId() {
    if (this.student != null) {
      return this.student.getStudentId();
    }
    return null;
  }

  // 상품 ID를 JSON에 포함시키기 위한 메서드
  public Integer getProductId() {
    if (this.product != null) {
      return this.product.getProductId();
    }
    return null;
  }
}