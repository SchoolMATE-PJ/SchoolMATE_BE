package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SchoolScheduleRes {
    @JsonProperty("SchoolSchedule") // 응답 JSON의 최상위 키
    private List<SchoolScheduleWrapper> schoolSchedule;
}
