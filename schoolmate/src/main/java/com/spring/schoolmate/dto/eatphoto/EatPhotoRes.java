package com.spring.schoolmate.dto;

import com.spring.schoolmate.entity.EatPhoto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EatPhotoRes {

  private Integer eatphotoId;
  private Long studentId;
  private String eatimageUrl;
  private LocalDateTime eatuploadedAt;

  public static EatPhotoRes fromEntity(EatPhoto eatPhoto) {
    return EatPhotoRes.builder()
      .eatphotoId(eatPhoto.getEatphotoId())
      .studentId(eatPhoto.getStudent().getStudentId())
      .eatimageUrl(eatPhoto.getEatimageUrl())
      .eatuploadedAt(eatPhoto.getEatuploadedAt())
      .build();
  }
}