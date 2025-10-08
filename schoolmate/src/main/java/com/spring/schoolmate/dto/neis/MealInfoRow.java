package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MealInfoRow {
    @JsonProperty("MLSV_YMD")
    private String mealDate; // 급식일자 (YYYYMMDD)

    @JsonProperty("MMEAL_SC_NM")
    private String mealName; // 조식/중식/석식 명칭

    @JsonProperty("DDISH_NM")
    private String dishName; // 요리명 및 알레르기 정보 (예: 쌀밥<5.13>)

    @JsonProperty("CAL_INFO")
    private String calorieInfo; // 칼로리 정보

    @JsonProperty("NTR_INFO")
    private String nutritionInfo; // 영양 정보
}
