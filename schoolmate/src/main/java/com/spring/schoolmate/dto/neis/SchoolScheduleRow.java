package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SchoolScheduleRow {

    @JsonProperty("SCHUL_NM")
    private String schoolName; // 학교명

    @JsonProperty("AA_YMD")
    private String scheduleDate; // 학사일자 (YYYYMMDD)

    @JsonProperty("EVENT_NM")
    private String eventName; // 행사명

/*    @JsonProperty("EVENT_CNTNT")
    private String eventConetnt; // 행사내용*/
}

