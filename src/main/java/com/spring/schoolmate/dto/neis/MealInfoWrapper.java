package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MealInfoWrapper {

    @JsonProperty("head")
    private List<Object> head; // API 상태/카운트 정보

    @JsonProperty("row")
    private List<MealInfoRow> row; // 실제 급식 메뉴 데이터 리스트
}
