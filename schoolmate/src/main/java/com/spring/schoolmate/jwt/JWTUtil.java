package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Student;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// JWT 정보 검증 및 생성
@Component
@Slf4j
public class JWTUtil {

    private SecretKey secretKey;//Decode한 secret key를 담는 객체

    //application.properties에 있는 미리 Base64로 Encode된 Secret key를 가져온다
    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //검증 Id
    public Long getId(String token) {
        log.info("getId(String token) :: call");
        Long rId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
        log.info("getId(String token) id = {}", rId);
        return rId;
    }

    //검증 Email
    public String getEmail(String token) {
        log.info("getEmail(String token) :: call");
        String rEmail = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
        log.info("getEmail(String token)  re = {}" ,rEmail);
        return rEmail;
    }

    //검증 Role
    public String getRole(String token) {
        log.info("getRole(String token) :: call");
        String rRole = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
        log.info("getRole(String token) role = {}", rRole);
        return rRole;
    }

    //검증 Expired
    public Boolean isExpired(String token) {
        log.info("isExpired(String token) :: call");
        try {
            boolean rExpired = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
            log.info("isExpired(String token) expired = {}", rExpired);
            return rExpired;
        } catch (Exception e) {
            log.warn("토큰 검증 오류: {}", e.getMessage());
            return true;
        }
    }

    public String createJwt(Student student, String email, String role, Long expiredMs) {
        log.info("createJwt  call");
        return Jwts.builder()
                .claim("id", student.getStudentId()) //아이디
                .claim("email", student.getEmail()) //멤버번호
                .claim("username", student.getName()) //이름
                .claim("role", role) //Role
                .issuedAt(new Date(System.currentTimeMillis())) //현재로그인된 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) //만료시간
                .signWith(secretKey)
                .compact();
    }

}