package com.spring.schoolmate.dto.neis;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
@Data
public class TimetableRes {
    private String timetableDate;
    private String schoolName; // 학교명
    private String period; // 교시
    private String subjectName; // 수업내용
    private String departmentName; // 학과명 :: 고등학교 학과별 수업 시간표 다름
}
