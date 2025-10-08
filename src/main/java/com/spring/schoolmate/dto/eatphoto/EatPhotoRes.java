package com.spring.schoolmate.dto.eatphoto;

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

  // ⭐️ 1. 학교 이름 필드 추가 ⭐️
  private String schoolName;

  public static EatPhotoRes fromEntity(EatPhoto eatPhoto) {
    // 학교 이름 기본값 설정
    String schoolName = "학교 정보 없음";

    // ⭐️ 2. Profile 엔티티를 통해 학교 이름 가져오기 ⭐️
    // EatPhoto -> Student -> Profile 관계를 따라 접근
    if (eatPhoto.getStudent() != null && eatPhoto.getStudent().getProfile() != null) {
      schoolName = eatPhoto.getStudent().getProfile().getSchoolName();
    }

    return EatPhotoRes.builder()
      .eatphotoId(eatPhoto.getEatphotoId())
      .studentId(eatPhoto.getStudent().getStudentId())
      .eatimageUrl(eatPhoto.getEatimageUrl())
      .eatuploadedAt(eatPhoto.getEatuploadedAt())
      // ⭐️ 3. DTO 빌더에 schoolName 포함 ⭐️
      .schoolName(schoolName)
      .build();
  }
}