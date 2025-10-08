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
    private String schoolCode; // 학교코드

    @JsonProperty("SCHUL_NM")
    private String schoolName; // 학교명

    @JsonProperty("LCTN_SC_NM")
    private String locationName; // 시도명

    @JsonProperty("ORG_RDNMA")
    private String loadName; // 도로명주소

    @JsonProperty("HS_SC_NM")
    private String schoolType; // 고등학교구분명 :: 일반고, 특성화고
}
