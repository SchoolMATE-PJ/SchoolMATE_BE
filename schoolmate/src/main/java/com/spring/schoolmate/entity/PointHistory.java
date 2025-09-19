package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "point_history")
public class PointHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ph_id")
  private Long phId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Column(name = "transaction_type", length = 50, nullable = false)
  private String transactionType;

  @Column(name = "amount", nullable = false)
  private Integer amount;

  @Column(name = "balance_after", nullable = false)
  private Integer balanceAfter;

  @Column(name = "reference_type", length = 50, nullable = true)
  private String referenceType;

  @Column(name = "reference_id", nullable = true)
  private Long referenceId;

  @Column(name = "created_at", nullable = false)
  private Timestamp createdAt;

  @Column(name = "expires_at", nullable = true)
  private Timestamp expiresAt;
}