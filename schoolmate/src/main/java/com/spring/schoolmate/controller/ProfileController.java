package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.profile.MyProfileRes;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.dto.profile.ProfileUpdateReq;
import com.spring.schoolmate.security.CustomStudentDetails;
import com.spring.schoolmate.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

}
