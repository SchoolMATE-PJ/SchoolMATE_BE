package com.spring.schoolmate.dto.neis;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
@Data
public class TimetableRes {
    private String timetableDate;
    private String period;
    private String subjectName;
}
