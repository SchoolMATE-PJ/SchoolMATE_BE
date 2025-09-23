package com.spring.schoolmate.dto.eatphotovisionscore;

import com.spring.schoolmate.entity.EatphotoVisionScore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EatphotoVisionScoreRes {

  /**
   * EatPhotoVisionScoreReq가 필요가 없는 이유
   * 요청 주체가 클라이언트가 아니다:
   * -> EatphotoVisionScore의 데이터는 프론트엔드에서 직접 전송하는 것이 아니라,
   * -> 서버가 Vision AI API와 통신하여 얻은 결과
   * 서버 내부의 데이터 흐름:
   * 데이터는 "Vision AI 응답" -> "서비스 계층 가공" -> "엔티티 변환 및 DB 저장" 순으로 흐른다.
   * 이 과정에서 별도의 요청 DTO를 만드는 것은 불필요한 추상화일 수 있다.
   */

  /**
   * EatPhotoVisionScoreRes가 필요한 이유
   * 서버가 클라이언트에게 AI 분석 결과를 응답으로 보낼 때 사용
   */

  private Integer eatphotoVisionScoreId;
  private Integer eatphotoId; // 분석 대상 급식 사진 ID
  private String labelName; // AI가 식별한 라벨명
  private Float score; // AI가 부여한 신뢰도 점수

  public static EatphotoVisionScoreRes fromEntity(EatphotoVisionScore eatphotoVisionScore) {
    return EatphotoVisionScoreRes.builder()
      .eatphotoVisionScoreId(eatphotoVisionScore.getEatphotoVisionScoreId())
      .eatphotoId(eatphotoVisionScore.getEatphoto().getEatphotoId())
      .labelName(eatphotoVisionScore.getVisionLabel().getLabelName())
      .score(eatphotoVisionScore.getScore())
      .build();
  }
}