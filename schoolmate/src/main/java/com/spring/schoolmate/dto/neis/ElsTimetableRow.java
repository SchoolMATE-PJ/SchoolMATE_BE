package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ElsTimetableRow {
    @JsonProperty("ATPT_OFCDC_SC_NM")
    private String officeName;

    @JsonProperty("SCHUL_NM")
    private String schoolName;

    @JsonProperty("AY")
    private String schoolYear;

    @JsonProperty("ALL_TI_YMD")
    private String timetableDate;

    @JsonProperty("GRADE")
    private String grade;

    @JsonProperty("CLASS_NM")
    private String classNo;

    @JsonProperty("PERIO")
    private String period;

    @JsonProperty("ITRT_CNTNT")
    private String subjectName;
}
