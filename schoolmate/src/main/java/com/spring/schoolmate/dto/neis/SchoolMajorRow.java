package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SchoolMajorRow {
    @JsonProperty("MAJOR_NM")
    private String majorName; // 학과명
}
