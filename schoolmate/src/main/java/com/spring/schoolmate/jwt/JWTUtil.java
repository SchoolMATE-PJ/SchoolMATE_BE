package com.spring.schoolmate.jwt;

import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.OAuth2CustomUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // <--- [수정] javax -> jakarta
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final long expirationTime = 60 * 60 * 1000L; // 1시간
    private final long tempExpirationTime = 10 * 60 * 1000L; // 10분

    // [수정] PlaceholderResolutionException 방지를 위한 디폴트 값 명시. (유지)
    public JWTUtil(@Value("${jwt.secret:Q1lWd2hYUnN5Y3p3TVJ5bVdWc1FYTXlKMW9mZEp0QnFvVmpLckd0VzI0OA==}") String secret) {
        byte[] keyBytes;
        boolean decodedFromBase64 = false;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
            decodedFromBase64 = true;
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            String hint = decodedFromBase64
              ? "Base64로 디코딩한 결과 키 길이가 32바이트 미만입니다."
              : "raw 문자열 바이트 길이가 32바이트 미만입니다.";
            log.error("JWT secret이 너무 짧습니다 (최소 32바이트 필요). " + hint); // 🚨 [수정 3] log.error를 throw 직전에 명확하게 배치
            throw new IllegalArgumentException("JWT secret이 너무 짧습니다. " + hint);
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    private void postConstruct() {
        log.info("JWT secretKey initialized (encoded length = {} bytes)", secretKey.getEncoded().length);
    }

    // [수정] JJWT 0.12.x 버전에 맞게 parseClaimsJws 대신 parseSignedClaims 사용
    private Claims parseClaims(String token) {
        return Jwts.parser()
          .verifyWith((SecretKey) secretKey) // 🚨 [수정 4] Key 타입을 SecretKey로 캐스팅하여 명시적으로 사용
          .build()
          .parseSignedClaims(token)
          .getPayload();
    }

    public Long getId(String token) {
        return parseClaims(token).get("id", Long.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // [수정] issuedAt(Date) 대신 setIssuedAt(Date) 또는 issuedAt(Date) 패턴 사용
    public String createJwt(Student student) {
        return Jwts.builder()
          .claims()
          .add("id", student.getStudentId())
          .add("email", student.getEmail())
          .add("role", student.getRole().getRoleName().name())
          .issuedAt(new Date(System.currentTimeMillis())) // <--- [확인] 이 방식이 0.12.x에서 더 안정적입니다.
          .expiration(new Date(System.currentTimeMillis() + expirationTime))
          .and()
          .signWith(secretKey, Jwts.SIG.HS256)
          .compact();
    }

    public String createJwt(Admin admin) {
        return Jwts.builder()
          .claims()
          .add("id", admin.getAdminId())
          .add("email", admin.getEmail())
          .add("role", admin.getRole().name())
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + expirationTime))
          .and()
          .signWith(secretKey, Jwts.SIG.HS256)
          .compact();
    }

    public String createTempSignupToken(OAuth2CustomUser oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        return Jwts.builder()
          .claims()
          .add("socialId", oAuth2User.getName())
          .add("provider", oAuth2User.getRegistrationId())
          .add("name", attributes.get("nickname") != null ? attributes.get("nickname") : attributes.get("name"))
          .add("role", "TEMP_USER")
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + tempExpirationTime))
          .and()
          .signWith(secretKey, Jwts.SIG.HS256)
          .compact();
    }

    public Map<String, Object> getOAuth2AttributesFromTempToken(String tempToken) {
        Claims claims = parseClaims(tempToken);
        return claims;
    }
}