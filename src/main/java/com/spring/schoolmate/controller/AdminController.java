package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.admin.AdminProfileUpdateReq;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.service.AdminProfileService;
import io.swagger.v3.oas.annotations.Operation; // Operation 어노테이션 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 전용 컨트롤러.
 * 학생 프로필 수정, 전체 학생 목록 및 포인트 조회 기능을 제공합니다.
 */
@Tag(name = "Admin (Students)", description = "관리자 전용: 학생 프로필 및 목록 조회/수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

  private final AdminProfileService adminProfileService;

  /**
   * 1. 특정 학생의 정보 및 포인트 수정 API
   * PUT /api/admin/students/{studentId}/profile
   */
  @Operation(
    summary = "특정 학생 프로필/포인트 수정",
    description = "관리자가 특정 학생의 이름, 전화번호, 학교, 현재 포인트 등을 수정합니다."
  )
  @PutMapping("/students/{studentId}/profile")
  public ResponseEntity<ProfileRes> updateStudentProfile(
    @PathVariable Long studentId,
    @RequestBody AdminProfileUpdateReq req) {

    ProfileRes updatedProfile = adminProfileService.updateStudentProfile(studentId, req);
    return ResponseEntity.ok(updatedProfile);
  }

  /**
   * 2. 모든 학생의 목록과 프로필, 현재 보유 포인트를 조회하는 API
   * GET /api/admin/students
   * Pageable을 인자로 받아 페이징 및 검색 조건을 처리하도록 수정
   */
  @Operation(
    summary = "전체 학생 목록 및 프로필 조회 (페이지네이션/검색)",
    description = "전체 학생 목록과 프로필 및 현재 포인트를 페이징하여 조회합니다. 이름, 전화번호, 학교를 이용한 검색이 가능합니다."
  )
  @GetMapping("/students")
  public ResponseEntity<Page<ProfileRes>> getStudents(
    // Pageable 객체를 사용하여 page, size, sort 파라미터를 자동 바인딩
    Pageable pageable,
    // 검색 조건 추가 (프론트엔드의 searchBy.key에 대응)
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String phone,
    @RequestParam(required = false) String school
  ) {
    Page<ProfileRes> studentPage = adminProfileService.getStudentProfiles(
      pageable, name, phone, school
    );
    return ResponseEntity.ok(studentPage);
  }
}