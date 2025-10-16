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

/**
 * 학교 정보 검색 및 조회 컨트롤러.
 * NEIS API를 사용하여 회원가입 및 프로필 설정에 필요한 학교, 학과, 학급 정보를 제공합니다.
 */
@Tag(name = "School Search (NEIS)", description = "회원가입, 프로필 수정에 필요한 학교 정보 검색 및 조회 API")
@RestController
@RequestMapping("/api/school-search")
@RequiredArgsConstructor
public class SchoolSearchController {
    private final NeisApiService neisApiService;

    @GetMapping
    @Operation(
      summary = "학교 검색",
      description = "학교 이름과 학교급(초/중/고)을 기준으로 NEIS API에서 학교 목록을 검색합니다. 회원가입 시 학교 코드 획득에 사용됩니다."
    )
    public ResponseEntity<List<SchoolInfoRow>> searchSchool(
      @Parameter(description = "검색할 학교 이름", required = true) @RequestParam String schoolName,
      @Parameter(description = "학교급 (초등학교, 중학교, 고등학교)", required = true) @RequestParam String schoolLevel) {

        List<SchoolInfoRow> schoolList = neisApiService.searchSchool(schoolName, schoolLevel);
        return ResponseEntity.ok(schoolList);
    }

    @GetMapping("/majors")
    @Operation(
      summary = "학과 정보 조회",
      description = "학교 코드와 교육청 코드를 이용해 해당 학교에 개설된 학과 목록을 조회합니다. (주로 고등학교용)"
    )
    public ResponseEntity<List<String>> getSchoolMajors(
      @Parameter(description = "시도교육청코드", required = true) @RequestParam String educationOfficeCode,
      @Parameter(description = "학교 행정표준코드", required = true) @RequestParam String schoolCode) {

        List<String> majorList = neisApiService.findMajorsBySchoolType(educationOfficeCode, schoolCode);
        return ResponseEntity.ok(majorList);
    }

    @GetMapping("/class-info")
    @Operation(
      summary = "학급 정보 조회",
      description = "특정 학교의 특정 학년(선택적으로 학과)에 개설된 학급 목록을 조회합니다. 회원가입 시 학급 번호 획득에 사용됩니다."
    )
    public ResponseEntity<List<ClassInfoRow>> getClassInfo(
      @Parameter(description = "시도교육청코드", required = true) @RequestParam String educationOfficeCode,
      @Parameter(description = "학교 행정표준코드", required = true) @RequestParam String schoolCode,
      @Parameter(description = "학년", required = true) @RequestParam String grade,
      @Parameter(description = "학교급 (초등학교, 중학교, 고등학교)", required = true) @RequestParam String schoolLevel,
      @Parameter(description = "학과명 (선택)") @RequestParam(required = false) String majorName) {

        // schoolLevel 파라미터를 서비스로 전달
        List<ClassInfoRow> classList = neisApiService.getClassInfo(educationOfficeCode, schoolCode, grade, schoolLevel, majorName);
        return ResponseEntity.ok(classList);
    }
}