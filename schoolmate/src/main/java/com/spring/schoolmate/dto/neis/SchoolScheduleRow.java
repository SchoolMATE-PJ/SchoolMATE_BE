package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SchoolScheduleRow {
    @JsonProperty("ATPT_OFCDC_SC_CODE")
    private String educationOfficeCode; // 시도교육청코드

    @JsonProperty("SD_SCHUL_CODE")
    private String schoolCode; // 학교코드 (행정표준코드)

    @JsonProperty("SCHUL_NM")
    private String schoolName; // 학교명

    @JsonProperty("ALL_TI_YMD")
    private String scheduleDate; // 학사일자 (YYYYMMDD)

    @JsonProperty("EVENT_NM")
    private String eventName; // 행사명

    @JsonProperty("ONE_GRADE_EVENT_YN")
    private String firstGradeEventYn; // 1학년 행사 여부 (Y/N)

    @JsonProperty("TW_GRADE_EVENT_YN")
    private String secondGradeEventYn; // 2학년 행사 여부 (Y/N)

    @JsonProperty("THREE_GRADE_EVENT_YN")
    private String thirdGradeEventYn; // 3학년 행사 여부 (Y/N)

    @JsonProperty("LOAD_DTM")
    private String loadDateTime; // 자료 로드 일시 (YYYYMMDDHHMMSS)
}
