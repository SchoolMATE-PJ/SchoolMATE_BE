package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ElsTimetableRes{
    @JsonProperty("elsTimetable") // 초등학교 시간표 응답 키
    private List<ElsTimetableWrapper> elsTimetable;
}
