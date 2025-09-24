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

    private final SecretKey secretKey;//Decode한 secret key를 담는 객체
    private final Long expirationTime;
    //application.properties에 있는 미리 Base64로 Encode된 Secret key를 가져온다
    public JWTUtil(@Value("${spring.jwt.secret}")String secret,
                   @Value("${spring.jwt.expiration-time}")Long expirationTime) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.expirationTime = expirationTime;
    }

    //검증 Id
    public Long getStudentId(String token) {
        log.info("getStudentId(String token) :: call");
        Long rId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("studentId", Long.class);
        log.info("getStudentId(String token) id = {}", rId);
        return rId;
    }

    //검증 Email
    public String getEmail(String token) {
        log.info("getEmail(String token) :: call");
        String rEmail = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
        log.info("getEmail(String token)  re = {}" ,rEmail);
        return rEmail;
    }

    //검증 StudentName
    public String getStudentName(String token) {
        log.info("getStudentName(String token)  call");
        String rStudentName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("studentName", String.class);
        log.info("getStudentName(String token)  rStudentName = {}" ,rStudentName);
        return rStudentName;
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

    public String createJwt(Student student) {
        log.info("createJwt  call");
        return Jwts.builder()
                .claim("studentId", student.getStudentId()) // PK
                .claim("email", student.getEmail()) // 이메일 :: 로그인 시 입력할 아이디
                .claim("studentName", student.getName()) // 이름
                .claim("role", student.getRole().getRoleName().toString()) //Role
                .issuedAt(new Date(System.currentTimeMillis())) //현재로그인된 시간
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) //만료시간
                .signWith(secretKey)
                .compact();
    }

}