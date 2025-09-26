package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class HisTimetableRes {
    @JsonProperty("hisTimetable") // 고등학교 시간표 응답 JSON의 최상위 키
    private List<TimetableWrapper> hisTimetable;
}
