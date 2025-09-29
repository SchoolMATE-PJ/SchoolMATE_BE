package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.neis.*;
import com.spring.schoolmate.service.NeisApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "NEIS API", description = "NEIS Open API 연동 관련")
@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Slf4j
public class SchoolController {

    private final NeisApiService neisApiService;

    @Operation(summary = "학교 검색", description = "학교명과 학교급으로 NEIS에서 학교 목록을 검색합니다.")
    @GetMapping
    public ResponseEntity<List<SchoolInfoRow>> searchSchool(
            @Parameter(description = "검색할 학교 이름", required = true)
            @RequestParam String schoolName,


            @Parameter(description = "학교급 (초등학교, 중학교, 고등학교)", required = true)
            @RequestParam String schoolLevel) {

        log.info(">>>>> SchoolController searchSchool called! schoolName={}, schoolLevel={}", schoolName, schoolLevel);
        List<SchoolInfoRow> schoolList = neisApiService.searchSchool(schoolName, schoolLevel);
        return ResponseEntity.ok(schoolList);
    }

    // 학과 정보 조회 API
    @Operation(summary = "학과 정보 조회", description = "특정 학교의 학과 목록을 조회합니다. (주로 특성화고/마이스터고용)")
    @GetMapping("/majors")
    public ResponseEntity<List<SchoolMajorRow>> getSchoolMajors(
            @Parameter(description = "시도교육청코드", required = true)
            @RequestParam String educationOfficeCode,

            @Parameter(description = "학교 행정표준코드", required = true)
            @RequestParam String schoolCode) {

        List<SchoolMajorRow> majorList = neisApiService.getSchoolMajors(educationOfficeCode, schoolCode);
        return ResponseEntity.ok(majorList);
    }

    // 학급 정보 조회 API
    @Operation(summary = "학년별 반 정보 조회", description = "특정 학교, 특정 학년의 반 목록을 조회합니다.")
    @GetMapping("/class-info")
    public ResponseEntity<List<ClassInfoRow>> getClassInfo(
            @Parameter(description = "시도교육청코드", required = true)
            @RequestParam String educationOfficeCode,

            @Parameter(description = "학교 행정표준코드", required = true)
            @RequestParam String schoolCode,

            @Parameter(description = "학년", required = true, example = "1")
            @RequestParam String grade) {

        List<ClassInfoRow> classList = neisApiService.getClassInfo(educationOfficeCode, schoolCode, grade);
        return ResponseEntity.ok(classList);
    }

    // 급식 정보 조회 API
    @Operation(summary = "급식 정보 조회", description = "특정 학교, 특정 날짜의 급식 정보를 조회합니다.")
    @GetMapping("/meals")
    public ResponseEntity<List<MealInfoRow>> getMealInfo(
            @Parameter(description = "시도교육청코드", required = true)
            @RequestParam String educationOfficeCode,

            @Parameter(description = "학교 행정표준코드", required = true)
            @RequestParam String schoolCode,

            @Parameter(description = "조회할 날짜 (YYYYMMDD 형식)", required = true, example = "20250926")
            @RequestParam String date) {

        List<MealInfoRow> mealList = neisApiService.getMealService(educationOfficeCode, schoolCode, date);
        return ResponseEntity.ok(mealList);
    }

    // 학사일정 조회 API
    @Operation(summary = "학사일정 조회", description = "특정 학교의 기간 내 학사일정을 조회합니다.")
    @GetMapping("/schedules")
    public ResponseEntity<List<SchoolScheduleRow>> getSchoolSchedule(
            @Parameter(description = "시도교육청코드", required = true)
            @RequestParam String educationOfficeCode,

            @Parameter(description = "학교 행정표준코드", required = true)
            @RequestParam String schoolCode,

            @Parameter(description = "조회 시작일 (YYYYMMDD 형식)", required = true, example = "20250901")
            @RequestParam String startDate,

            @Parameter(description = "조회 종료일 (YYYYMMDD 형식)", required = true, example = "20250930")
            @RequestParam String endDate) {

        List<SchoolScheduleRow> scheduleList = neisApiService.getSchoolSchedule(educationOfficeCode, schoolCode, startDate, endDate);
        return ResponseEntity.ok(scheduleList);
    }

    // ✅ [신규] 시간표 조회 API
    @Operation(summary = "통합 시간표 조회", description = "학교급에 따라 초/중/고 시간표를 조회합니다.")
    @GetMapping("/timetables")
    public ResponseEntity<List<TimetableRow>> getTimetable(
            @Parameter(description = "학교급 (초등학교, 중학교, 고등학교)", required = true)
            @RequestParam String schoolLevel,

            @Parameter(description = "시도교육청코드", required = true)
            @RequestParam String educationOfficeCode,

            @Parameter(description = "학교 행정표준코드", required = true)
            @RequestParam String schoolCode,

            @Parameter(description = "조회할 날짜 (YYYYMMDD 형식)", required = true, example = "20250926")
            @RequestParam String date,

            @Parameter(description = "학년", required = true, example = "1")
            @RequestParam String grade,

            @Parameter(description = "반", required = true, example = "3")
            @RequestParam String classNo,

            @Parameter(description = "학과명 (특성화고만 해당, 초,중학교에는 필요없음)", required = false)
            @RequestParam(required = false) String majorName) {

        List<TimetableRow> timetableList = neisApiService.getTimetable(schoolLevel, educationOfficeCode, schoolCode, date, grade, classNo, majorName);
        return ResponseEntity.ok(timetableList);
    }
}