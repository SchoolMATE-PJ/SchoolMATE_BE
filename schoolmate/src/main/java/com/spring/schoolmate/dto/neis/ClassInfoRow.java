package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClassInfoRow {
    @JsonProperty("AY")
    private String schoolYear; // 학년도

    @JsonProperty("GRADE")
    private String grade; // 학년

    @JsonProperty("CLASS_NM")
    private String className; // 반 이름
}
