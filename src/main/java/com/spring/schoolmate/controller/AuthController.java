package com.spring.schoolmate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.schoolmate.dto.auth.ExternalSignUpReq;
import com.spring.schoolmate.dto.auth.ExternalSignUpRes;
import com.spring.schoolmate.dto.auth.SignUpReq;
import com.spring.schoolmate.dto.external.ExternalAccountReq;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.StudentRepository;
import com.spring.schoolmate.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"*"} , maxAge = 6000)
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 일반 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpReq request) {
        // AuthService에 회원가입 처리를 위임하고, 결과를 반환받습니다.
        // 성공 시 201 Created 상태 코드와 함께 생성된 사용자 정보를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    /**
     * 외부 계정(소셜)으로 시작하여, 추가 정보를 받아 최종 회원가입을 처리하는 API입니다.
     * @param request 카카오 정보를 포함하여 사용자가 최종 입력한 정보가 담긴 DTO
     */
    /**
     * 외부 계정(소셜)으로 시작하여, 추가 정보를 받아 최종 회원가입을 처리하는 API입니다.
     */
    @PostMapping("/signup/social")
    public ResponseEntity<?> externalSignUp(@RequestBody ExternalSignUpReq request) {
        try {
            // try 블록 안에서 정상적인 로직을 시도합니다.
            ExternalSignUpRes response = authService.externalSignUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (JsonProcessingException e) {
            // catch 블록으로 JsonProcessingException 예외를 잡습니다.
            //    이 예외는 주로 'tempToken'이 유효하지 않거나 깨졌을 때 발생합니다.
            log.error("소셜 회원가입 중 임시 토큰 처리 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().body("유효하지 않은 요청입니다. (Invalid token)");

        } catch (Exception e) {
            // 그 외 다른 예외가 발생했을 경우를 대비합니다.
            log.error("소셜 회원가입 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }

    // 이메일 중복 확인 API
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        if (authService.existCheckEmail(email)) {
            // 중복이면 409 Conflict (충돌) 상태 코드를 반환합니다.
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
        }
        // 중복이 아니면 200 OK (성공) 상태 코드를 반환합니다.
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        if (authService.existCheckNickname(nickname)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 닉네임입니다.");
        }
        return ResponseEntity.ok("사용 가능한 닉네임입니다.");
    }

    // 전화번호 중복 확인 API
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        if (authService.existCheckPhone(phone)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 전화번호입니다.");
        }
        return ResponseEntity.ok("사용 가능한 전화번호입니다.");
    }

}
