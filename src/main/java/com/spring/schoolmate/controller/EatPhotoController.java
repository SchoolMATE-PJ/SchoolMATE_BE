package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.eatphoto.EatPhotoRes;
import com.spring.schoolmate.entity.EatPhoto;
import com.spring.schoolmate.service.EatPhotoService;
import io.swagger.v3.oas.annotations.Operation; // Operation 어노테이션 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 급식 사진 조회 및 관리 컨트롤러.
 * 학생들의 급식 인증 사진 목록을 조회하는 기능을 제공합니다. (주로 관리자용)
 */
@Tag(name = "Meal Photos Retrieval (Admin)", description = "학생들이 업로드한 급식 사진 조회 및 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/photos")
public class EatPhotoController {

  private final EatPhotoService eatPhotoService;

  public EatPhotoController(EatPhotoService eatPhotoService) {
    this.eatPhotoService = eatPhotoService;
  }

  @Operation(
    summary = "특정 학생의 급식 사진 전체 조회",
    description = "지정된 학생 ID({studentId})가 업로드한 모든 급식 인증 사진 목록을 조회합니다. (원시 Entity 반환 가능성 있음)"
  )
  @GetMapping("/students/{studentId}")
  public ResponseEntity<?> getPhotoByStudentId(@PathVariable Integer studentId) {
    List<EatPhoto> photos = eatPhotoService.getPhotoByStudentId(studentId);
    return ResponseEntity.ok(photos);
  }

  // ⭐️ 반환 타입과 메서드 호출 결과 타입을 DTO로 변경 ⭐️
  @Operation(
    summary = "전체 학생의 모든 급식 사진 조회",
    description = "모든 학생들이 업로드한 급식 인증 사진 목록을 최신순으로 조회합니다. (주로 관리자 모니터링용)"
  )
  @GetMapping("/allStudentsPhotos")
  public ResponseEntity<List<EatPhotoRes>> getAllStudentPhotos() {
    List<EatPhotoRes> photos = eatPhotoService.getAllStudentPhotos();
    return ResponseEntity.ok(photos);
  }
}