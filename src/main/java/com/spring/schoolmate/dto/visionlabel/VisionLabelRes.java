package com.spring.schoolmate.dto.visionlabel;

import com.spring.schoolmate.entity.VisionLabel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class VisionLabelRes {

  private Integer labelId; // 클라이언트에게 노출할 라벨의 고유 식별자
  private String labelName; // 클라이언트가 사용자 인터페이스에 표시할 라벨명

  /**
   *
   * @param visionLabel 엔터티
   * @return VisionLabelRes DTO 객체
   */
  public static VisionLabelRes fromEntity(VisionLabel visionLabel) {
    return VisionLabelRes.builder()
      .labelId(visionLabel.getLabelId())
      .labelName(visionLabel.getLabelName())
      .build();
  }
}
