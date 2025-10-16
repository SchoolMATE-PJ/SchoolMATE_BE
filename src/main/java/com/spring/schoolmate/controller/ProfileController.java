package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.profile.MyProfileRes;
import com.spring.schoolmate.dto.profile.ProfileUpdateReq;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Tag(name = "프로필 (Profile)", description = "사용자 프로필 정보 조회 및 수정 API")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<MyProfileRes> getMyProfile(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        log.info(">>>>> 프로필 정보 조회 API Call");
        Long currentStudentId = customStudentDetails.getStudent().getStudentId();

        // ProfileService로부터 MyProfileRes 타입의 응답을 받음
        MyProfileRes profileResponse = profileService.getMyProfileDetails(currentStudentId);

        log.info("<<<<< 프로필 조회 성공! Student ID: {}", currentStudentId);
        return ResponseEntity.ok(profileResponse);
    }


    @PutMapping("/me")
    @Operation(summary = "내 프로필 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<MyProfileRes> updateMyProfile(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails,
      @RequestBody ProfileUpdateReq request) {

        log.info(">>>>> 프로필 정보 수정 API Call");
        // 현재 로그인한 사용자의 ID를 가져옵니다.
        Long currentStudentId = customStudentDetails.getStudent().getStudentId();

        // ProfileService에 ID와 수정할 정보를 전달하여 프로필을 업데이트
        MyProfileRes updatedProfile = profileService.updateProfile(currentStudentId, request);
        log.info("<<<<< 프로필 수정 성공! Student ID: {}", currentStudentId);
        // 수정된 프로필 정보를 200 OK 상태와 함께 반환
        return ResponseEntity.ok(updatedProfile);
    }

    // 프로필 이미지 업로드
    @PostMapping("/upload-image")
    @Operation(summary = "프로필 이미지 업로드", description = "새 프로필 이미지를 업로드하고 URL을 DB에 저장합니다.")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails,
      // 파일 업로드 요청의 Body에서 'file'이라는 이름으로 MultipartFile을 받음
      @RequestPart("file") MultipartFile file) {

        Long studentId = customStudentDetails.getStudent().getStudentId();
        log.info(">>>>> 프로필 이미지 업로드 API Call. Student ID: {}", studentId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "업로드할 파일이 없습니다."));
        }

        // 서비스 호출: 파일 저장 및 DB URL 업데이트
        String newImageUrl = profileService.uploadProfileImage(studentId, file);

        log.info("<<<<< 프로필 이미지 업로드 성공! URL: {}", newImageUrl);
        // 새로 저장된 이미지 URL을 JSON 형태로 반환
        return ResponseEntity.ok(
          Map.of("profileImgUrl", newImageUrl, "message", "프로필 이미지가 성공적으로 업로드되었습니다.")
        );
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/delete-image")
    @Operation(summary = "프로필 이미지 삭제", description = "현재 프로필 이미지 URL을 DB에서 제거(null)하여 기본 이미지로 돌아갑니다.")
    public ResponseEntity<Map<String, String>> deleteProfileImage(
      @AuthenticationPrincipal CustomStudentDetails customStudentDetails) {

        Long studentId = customStudentDetails.getStudent().getStudentId();
        log.info(">>>>> 프로필 이미지 삭제 API Call. Student ID: {}", studentId);

        // 서비스 호출: DB의 URL 필드를 null로 업데이트
        profileService.deleteProfileImage(studentId);

        log.info("<<<<< 프로필 이미지 삭제 성공! Student ID: {}", studentId);
        return ResponseEntity.ok(
          Map.of("message", "프로필 이미지가 성공적으로 삭제(초기화)되었습니다.")
        );
    }
}