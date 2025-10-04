package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.neis.*;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.NeisApiService;
import com.spring.schoolmate.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Tag(name = "NEIS API", description = "NEIS Open API 연동 관련")
@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class SchoolController {

    private final NeisApiService neisApiService;
    private final ProfileService profileService;

    // 급식 정보 조회 API
    @GetMapping("/meal")
    @Operation(summary = "월간 급식 정보 조회", description = "시작 날짜를 기준으로 한달간의 급식 정보를 조회합니다.")
    public ResponseEntity<List<MealInfoRow>> getMealInfo(
            @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        log.info(">>>>> 월간 급식 정보 정보 조회 API Call");
        if (customStudentDetails == null) {
            log.error("인증 정보가 없습니다. (customStudentDetails is null)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("급식 정보 조회 API 시작. 사용자: {}", customStudentDetails.getUsername());
        
        ProfileRes userProfile = profileService.getProfile(customStudentDetails.getStudent().getStudentId());

        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalDate endDateAfterOneMonth = today.plusMonths(1); // 오늘로부터 한 달 뒤 날짜
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = today.format(formatter); // 시작일: 오늘
        String endDate = endDateAfterOneMonth.format(formatter); // 종료일: 한 달 뒤

        List<MealInfoRow> mealList = neisApiService.getMealInfo(
                userProfile.getScCode(),
                userProfile.getSchoolCode(),
                startDate,
                endDate
        );
        log.info("프로필 정보 확인 완료: scCode={}, schoolCode={}", userProfile.getScCode(), userProfile.getSchoolCode());
        return ResponseEntity.ok(mealList);
    }

    // 학사일정 조회 API
    @Operation(summary = "학사일정 조회", description = "특정 학교의 기간 내 학사일정을 조회합니다.")
    @GetMapping("/schedule")
    public ResponseEntity<List<SchoolScheduleRow>> getMySchoolSchedule(
            @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        log.info(">>>>> 학사일정 정보 조회 API Call");
        ProfileRes userProfile = profileService.getProfile(customStudentDetails.getStudent().getStudentId());

        LocalDate today = LocalDate.now();
        String startDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endDate = today.withDayOfMonth(today.lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<SchoolScheduleRow> scheduleList = neisApiService.getSchoolSchedule(
                userProfile.getScCode(),
                userProfile.getSchoolCode(),
                startDate,
                endDate
        );
        return ResponseEntity.ok(scheduleList);
    }

    // 시간표 조회 API
    @Operation(summary = "주간 시간표 조회", description = "특정 날짜를 기준으로 해당 주(월~금)의 시간표를 조회합니다. 날짜 미입력 시 현재 주를 기준으로 조회합니다.")
    @GetMapping("/timetable")
    public ResponseEntity<List<TimetableRes>> getMySchoolTimetable(
            @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        ProfileRes userProfile = profileService.getProfile(customStudentDetails.getStudent().getStudentId());
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = today.with(DayOfWeek.FRIDAY);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = monday.format(formatter);
        String endDate = friday.format(formatter);

        List<TimetableRes> timetableList = neisApiService.getTimetable(
                userProfile.getLevel(),
                userProfile.getScCode(),
                userProfile.getSchoolCode(),
                startDate, // 시작일 (월요일)
                endDate,
                String.valueOf(userProfile.getGrade()),
                String.valueOf(userProfile.getClassNo()),
                userProfile.getMajorName()
        );
        return ResponseEntity.ok(timetableList);
    }
}