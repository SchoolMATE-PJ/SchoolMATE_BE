package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Data
public class HisTimetableRes{
    @JsonProperty("hisTimetable") // 고등학교 시간표
    private List<HisTimetableWrapper> hisTimetable;
}
