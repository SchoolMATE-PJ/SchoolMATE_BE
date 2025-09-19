package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eatphoto_vision_scores")
public class EatphotoVisionScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer eatphotoVisionScoreId; // 점수 기록의 고유 식별자

  // VisionLabel 엔티티와의 관계 설정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "label_id", nullable = false)
  private VisionLabel visionLabel; // AI가 식별한 라벨

  // EatPhoto 엔티티와의 관계 설정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "eatphoto_id", nullable = false)
  private EatPhoto eatphoto; // AI 분석을 요청한 급식 사진

  @Column(name = "score", nullable = false)
  private Float score; // AI가 부여한 신뢰도 점수 (0.0 ~ 1.0)
}