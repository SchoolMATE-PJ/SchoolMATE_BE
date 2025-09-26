package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SchoolInfoRow {

    @JsonProperty("ATPT_OFCDC_SC_CODE")
    private String educationOfficeCode; // 시도교육청코드

    @JsonProperty("SD_SCHUL_CODE")
    private String schoolCode; // 행정표준코드

    @JsonProperty("SCHUL_NM")
    private String schoolName; // 학교명


}
