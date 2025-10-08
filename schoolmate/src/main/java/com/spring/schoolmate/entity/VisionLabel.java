package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vision_labels")
public class VisionLabel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer labelId; // 라벨 고유 식별자

  @Column(length = 100, nullable = false)
  private String labelName; // AI가 식별한 라벨명

  @Column(length = 5, nullable = true)
  private String languageCode; // 라벨의 언어 코드
}