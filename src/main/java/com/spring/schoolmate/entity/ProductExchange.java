package com.spring.schoolmate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.annotations.NotNull;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
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

  // 1. Student 참조 무시: 순환 참조 방지를 위해 유지. 학생 ID는 getter로 제공.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  @JsonIgnore // 학생 정보는 목록에서 불필요하므로 유지
  private Student student; // 학생 엔터티

  // 2. 💡 Product 참조 포함: 상품 이름, 포인트, 이미지가 프론트엔드로 전달되어야 하므로 @JsonIgnore 제거
  // 💡 FetchType을 LAZY에서 EAGER로 변경하여, ProductExchange 조회 시 Product 정보를 즉시 로딩
  @ManyToOne(fetch = FetchType.EAGER) // 👈 LAZY -> EAGER로 변경
  @JoinColumn(name = "product_id", nullable = false)
  // @JsonIgnore // 👈 이 어노테이션을 반드시 제거해야 상품 정보가 JSON 응답에 포함됨
  private Product product; // 상품 엔터티

  @CreatedDate // 교환 시점에 자동으로 현재 날짜/시간으로 설정됩니다.
  @Column(name = "exchange_date", nullable = false, updatable = false)
  private LocalDateTime exchangeDate;

  @NotNull // 이 테이블에서는 만료일이 필수입니다.
  @Column(name = "expiration_date", nullable = false)
  private LocalDateTime expirationDate;

  @Column(nullable = true)
  private Date usageDate; // 상품 사용 일자 (이름 변경: usedDate 대신 usageDate 사용)

  @Column(length = 20, nullable = true)
  private String exchangeCardStatus; // 교환 상품 사용 상태

  // 학생 ID를 JSON에 포함시키기 위한 메서드 (유지)
  public Long getStudentId() {
    if (this.student != null) {
      return this.student.getStudentId();
    }
    return null;
  }

  // 상품 ID를 JSON에 포함시키기 위한 메서드 (유지)
  public Integer getProductId() {
    if (this.product != null) {
      return this.product.getProductId();
    }
    return null;
  }
}