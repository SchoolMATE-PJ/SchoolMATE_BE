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
  private Long phId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Column(length = 50, nullable = false)
  private String transactionType;

  @Column(nullable = false)
  private Integer amount;

  @Column(nullable = false)
  private Integer balanceAfter;

  @Column(length = 50, nullable = true)
  private String referenceType;

  @Column(nullable = true)
  private Long referenceId;

  @Column(nullable = false)
  private Timestamp createdAt;

  @Column(nullable = true)
  private Timestamp expiresAt;
}