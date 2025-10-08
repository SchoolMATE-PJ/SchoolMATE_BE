package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eat_photos")
public class EatPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer eatphotoId; // 급식 사진의 고유 식별자

  // 학생 엔티티와의 관계 설정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student; // 급식 사진을 업로드한 학생

  @Column(length = 255, nullable = false)
  private String eatimageUrl; // 급식 사진 파일 URL

  @Column(nullable = false)
  private LocalDateTime eatuploadedAt; // 사진 업로드 시각
}