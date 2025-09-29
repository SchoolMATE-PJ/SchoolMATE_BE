package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Data
public class HisTimetableRow {

    @JsonProperty("ATPT_OFCDC_SC_NM")
    private String officeName;

    @JsonProperty("SCHUL_NM")
    private String schoolName;

    @JsonProperty("AY")
    private String schoolYear;

    @JsonProperty("SEM")
    private String semester;

    @JsonProperty("ALL_TI_YMD")
    private String timetableDate;

    @JsonProperty("DGHT_CRSE_SC_NM")
    private String courseName;

    @JsonProperty("ORD_SC_NM")
    private String seriesName;

    @JsonProperty("DDDEP_NM")
    private String majorName;

    @JsonProperty("GRADE")
    private String grade;

    @JsonProperty("CLASS_NM")
    private String classNo;

    @JsonProperty("PERIO")
    private String period;

    @JsonProperty("ITRT_CNTNT")
    private String subjectName;
}
