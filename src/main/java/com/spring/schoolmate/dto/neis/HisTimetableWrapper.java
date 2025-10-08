package com.spring.schoolmate.dto.neis;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Data
public class HisTimetableWrapper {
    @JsonProperty("head")
    private List<Object> head;

    @JsonProperty("row")
    private List<HisTimetableRow> row;
}
