package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.neis.ClassInfoRow;
import com.spring.schoolmate.dto.neis.SchoolInfoRow;
import com.spring.schoolmate.dto.neis.SchoolMajorRow;
import com.spring.schoolmate.service.NeisApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "학교 검색 API", description = "회원가입, 프로필 수정에 필요한 학교 정보 검색 API")
@RestController
@RequestMapping("/api/school-search")
@RequiredArgsConstructor
public class SchoolSearchController {
    private final NeisApiService neisApiService;

    @GetMapping
    @Operation(summary = "학교 검색")
    public ResponseEntity<List<SchoolInfoRow>> searchSchool(
            @Parameter(description = "검색할 학교 이름", required = true) @RequestParam String schoolName,
            @Parameter(description = "학교급 (초등학교, 중학교, 고등학교)", required = true) @RequestParam String schoolLevel) {

        List<SchoolInfoRow> schoolList = neisApiService.searchSchool(schoolName, schoolLevel);
        return ResponseEntity.ok(schoolList);
    }

    @GetMapping("/majors")
    @Operation(summary = "학과 정보 조회")
    public ResponseEntity<List<SchoolMajorRow>> getSchoolMajors(
            @Parameter(description = "시도교육청코드", required = true) @RequestParam String educationOfficeCode,
            @Parameter(description = "학교 행정표준코드", required = true) @RequestParam String schoolCode) {

        List<SchoolMajorRow> majorList = neisApiService.getSchoolMajors(educationOfficeCode, schoolCode);
        return ResponseEntity.ok(majorList);
    }

    @GetMapping("/class-info")
    @Operation(summary = "학급 정보 조회")
    public ResponseEntity<List<ClassInfoRow>> getClassInfo(
            @Parameter(description = "시도교육청코드", required = true) @RequestParam String educationOfficeCode,
            @Parameter(description = "학교 행정표준코드", required = true) @RequestParam String schoolCode,
            @Parameter(description = "학년", required = true) @RequestParam String grade,
            @Parameter(description = "학과명 (선택)") @RequestParam(required = false) String majorName) {

        List<ClassInfoRow> classList = neisApiService.getClassInfo(educationOfficeCode, schoolCode, grade, majorName);
        return ResponseEntity.ok(classList);
    }
}
