package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.neis.MealInfoRow;
import com.spring.schoolmate.dto.neis.SchoolScheduleRow;
import com.spring.schoolmate.dto.neis.TimetableRes;
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
import java.util.List;

/**
 * 학교 정보 (NEIS API 연동) 관련 컨트롤러.
 * 급식, 학사일정, 시간표 조회 기능을 제공합니다.
 */
@Tag(name = "NEIS API", description = "NEIS Open API 연동 관련 (급식, 학사일정, 시간표)")
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
    @Operation(summary = "월간 급식 정보 조회", description = "로그인된 학생의 학교 정보(시작 날짜: 오늘, 기간: 한 달)를 기준으로 급식 정보를 조회합니다.")
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
    @Operation(summary = "월간 학사일정 조회", description = "로그인된 학생의 학교 정보를 기준으로 특정 연/월의 학사일정을 조회합니다.")
    @GetMapping("/schedule")
    public ResponseEntity<List<SchoolScheduleRow>> getMySchoolSchedule(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails,
      @Parameter(description = "조회할 연도 (4자리)", example = "2025") @RequestParam int year,
      @Parameter(description = "조회할 월 (1~12)", example = "10") @RequestParam int month) { // @Parameter 추가

        log.info(">>>>> 학사일정 정보 조회 API Call");
        ProfileRes userProfile = profileService.getProfile(customStudentDetails.getStudent().getStudentId());

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        String startDate = start.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endDate = end.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        log.info("조회 대상 기간: {} ~ {}", startDate, endDate);

        List<SchoolScheduleRow> scheduleList = neisApiService.getSchoolSchedule(
          userProfile.getScCode(),
          userProfile.getSchoolCode(),
          startDate,
          endDate
        );
        return ResponseEntity.ok(scheduleList);
    }

    // 시간표 조회 API
    @Operation(summary = "주간 시간표 조회", description = "특정 날짜를 기준으로 해당 주(월~금)의 시간표를 조회합니다. 학생의 학년/반/학과 정보를 사용합니다.")
    @GetMapping("/timetable")
    public ResponseEntity<List<TimetableRes>> getMySchoolTimetable(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails,
      @Parameter(description = "조회 기준 날짜 (YYYY-MM-DD 형식)", example = "2025-10-06") @RequestParam String date) {

        ProfileRes userProfile = profileService.getProfile(customStudentDetails.getStudent().getStudentId());

        // 1. 프론트에서 받은 날짜 문자열을 LocalDate 객체로 변환
        LocalDate criteriaDate = LocalDate.parse(date);

        // 2. 해당 날짜가 속한 주의 월요일과 금요일을 계산
        LocalDate monday = criteriaDate.with(DayOfWeek.MONDAY);
        LocalDate friday = criteriaDate.with(DayOfWeek.FRIDAY);

        // 3. NEIS API가 요구하는 'yyyyMMdd' 형식으로 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = monday.format(formatter);
        String endDate = friday.format(formatter);

        // ======================= [디버깅 로그 추가] =======================
        log.info(">>>>> NEIS API 요청 파라미터 <<<<<");
        log.info("   - 학교이름: {}", userProfile.getSchoolName());
        log.info("   - 학교급: {}", userProfile.getLevel());
        log.info("   - 교육청코드: {}", userProfile.getScCode());
        log.info("   - 학교코드: {}", userProfile.getSchoolCode());
        log.info("   - 시작일: {}", startDate);
        log.info("   - 종료일: {}", endDate);
        log.info("   - 학년: {}", userProfile.getGrade());
        log.info("   - 반: {}", userProfile.getClassNo());
        log.info("   - 학과명: '{}'", userProfile.getMajorName());
        // ===============================================================

        // 4. NeisApiService 호출
        List<TimetableRes> timetableList = neisApiService.getTimetable(
          userProfile.getLevel(),
          userProfile.getScCode(),
          userProfile.getSchoolCode(),
          startDate,
          endDate,
          String.valueOf(userProfile.getGrade()),
          String.valueOf(userProfile.getClassNo()),
          userProfile.getMajorName()
        );
        return ResponseEntity.ok(timetableList);
    }
}