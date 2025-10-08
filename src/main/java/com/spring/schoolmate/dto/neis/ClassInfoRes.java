package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClassInfoRes {
    @JsonProperty("classInfo")
    private List<ClassInfoWrapper> classInfo;
}
