package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "point_history")
public class PointHistory {

    // Point History 고유 ID
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ph_id")
  private Long phId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  // 포인트 거래 종류
  @Column(name = "transaction_type", length = 50, nullable = false)
  private String tsType;

  // 변동된 포인트
  @Column(name = "amount", nullable = false)
  private Integer amount;

  // 거래 이후 최종 잔액
  @Column(name = "balance_after", nullable = false)
  private Integer balanceAfter;

  // 참조하는 타입 종류
  @Column(name = "ref_type", length = 50)
  private String refType;

  // 참조하는 데이터 고유 ID
  @Column(name = "ref_id")
  private Long refId;

  // 거래 발생 시각
  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  // 포인트 만료 시간
  @Column(name = "expires_at")
  private Timestamp expiresAt;
}