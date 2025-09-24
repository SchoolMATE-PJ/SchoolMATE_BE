package com.spring.schoolmate.controller;

import com.spring.schoolmate.dto.auth.ExternalSignUpReq;
import com.spring.schoolmate.dto.auth.SignUpReq;
import com.spring.schoolmate.dto.external.ExternalAccountReq;
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
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody SignUpReq request) {
        // AuthService에 회원가입 처리를 위임하고, 결과를 반환받습니다.
        // 성공 시 201 Created 상태 코드와 함께 생성된 사용자 정보를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    /**
     * 소셜 계정 연동 회원가입
     */
    @PostMapping("/signUp/social")
    public ResponseEntity<?> externalSignUp(@RequestBody ExternalSignUpReq request) {
        // AuthService에 소셜 회원가입 처리를 위임합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.externalSignUp(request));
    }

    /**
     * 소셜 로그인
     */
    @PostMapping("/login/social")
    public ResponseEntity<?> socialLogin(@RequestBody ExternalAccountReq request) {
        // AuthService에서 소셜 로그인을 처리하고 JWT를 발급받습니다.
        String token = authService.socialLogin(request);

        // 발급받은 JWT를 응답 헤더(Authorization)에 담아서 보냅니다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        // 성공 시 200 OK 상태 코드와 함께 헤더를 반환합니다.
        return ResponseEntity.ok().headers(headers).build();
    }

}
