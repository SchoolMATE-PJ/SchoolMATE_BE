package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TimetableRow {

    @JsonProperty("ATPT_OFCDC_SC_CODE")
    private String educationOfficeCode; // 시도교육청코드

    @JsonProperty("SD_SCHUL_CODE")
    private String schoolCode; // 학교코드 (행정표준코드)

    @JsonProperty("AY")
    private String schoolYear; // 학년도

    @JsonProperty("SEM")
    private String semester; // 학기

    @JsonProperty("ALL_TI_YMD")
    private String timetableDate; // 시간표 일자 (YYYYMMDD)

    @JsonProperty("GRADE")
    private String grade; // 학년

    @JsonProperty("CLASS_NM")
    private String classNo; // 반명

    @JsonProperty("PERIO")
    private String period; // 교시

    @JsonProperty("ITRT_CNTNT")
    private String subjectName; // 교과목명 (예: 국어, 수학)

    @JsonProperty("LOAD_DTM")
    private String loadDateTime; // 자료 로드 일시
}
