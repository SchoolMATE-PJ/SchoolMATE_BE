package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Data
public class HisTimetableRow {
    @JsonProperty("SCHUL_NM")
    private String schoolName; // 학교명

    @JsonProperty("AY")
    private String schoolYear; // 학년도

    @JsonProperty("SEM")
    private String Semester; // 학기

    @JsonProperty("ORD_SC_NM")
    private String affiliationName; // 계열명

    @JsonProperty("DDDEP_NM")
    private String departmentName; // 학과명

    @JsonProperty("ALL_TI_YMD")
    private String timetableDate; // 시간표 일자

    @JsonProperty("GRADE")
    private String grade; // 학년

    @JsonProperty("CLASS_NM")
    private String classNo; // 학급명

    @JsonProperty("PERIO")
    private String period; // 교시

    @JsonProperty("ITRT_CNTNT")
    private String subjectName; // 수업내용
}
