package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MisTimetableWrapper {
    @JsonProperty("head")
    private List<Object> head;

    @JsonProperty("row")
    private List<MisTimetableRow> row;
}
