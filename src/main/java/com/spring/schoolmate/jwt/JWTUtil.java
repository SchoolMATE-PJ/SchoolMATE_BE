package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.schoolmate.entity.Admin;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.security.OAuth2CustomUser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// JWT 정보 검증 및 생성
@Component
@Slf4j
public class JWTUtil {

    private final SecretKey secretKey;
    private final Long expirationTime;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JWTUtil(@Value("${spring.jwt.secret}")String secret,
                   @Value("${spring.jwt.expiration-time}")Long expirationTime) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.expirationTime = expirationTime;
    }

    public Long getId(String token) {
        String role = getRole(token);
        String idClaimName = "STUDENT".equals(role) ? "studentId" : "adminId";

        log.info("getId(String token) :: call");
        Long rId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(idClaimName, Long.class);
        log.info("getId(String token) id = {}", rId);
        return rId;
    }

    public String getEmail(String token) {
        log.info("getEmail(String token) :: call");
        String rEmail = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
        log.info("getEmail(String token)  re = {}" ,rEmail);
        return rEmail;
    }

    public String getName(String token) {
        log.info("getName(String token)  call");
        String rName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("studentName", String.class);
        if (rName == null) {
            rName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
        }
        log.info("getName(String token)  rName = {}" ,rName);
        return rName;
    }

    public String getRole(String token) {
        log.info("getRole(String token) :: call");
        String rRole = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
        log.info("getRole(String token) role = {}", rRole);
        return rRole;
    }

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
        log.info("createJwt(Student) call");
        return Jwts.builder()
          .claim("studentId", student.getStudentId())
          .claim("email", student.getEmail())
          .claim("studentName", student.getName())
          .claim("role", student.getRole().getRoleName().toString())
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + expirationTime))
          .signWith(secretKey)
          .compact();
    }

    public String createJwt(Admin admin) {
        log.info("createJwt(Admin) call");
        return Jwts.builder()
          .claim("adminId", admin.getAdminId())
          .claim("email", admin.getEmail())
          .claim("role", admin.getRole().toString())
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + expirationTime))
          .signWith(secretKey)
          .compact();
    }


    // 1. OAuth2SuccessHandler용 임시 토큰 생성
    public String createTempSignupToken(OAuth2CustomUser oAuth2User) throws JsonProcessingException {
        // 이 메서드는 UserNotRegisteredException을 사용하면 호출되지 않지만, 유지합니다.
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Unmodifiable Map 에러 방지용 복사
        Map<String, Object> modifiableAttributes = new HashMap<>(attributes);
        modifiableAttributes.put("provider", oAuth2User.getRegistrationId());

        String attributesJson = objectMapper.writeValueAsString(modifiableAttributes);

        return Jwts.builder()
          .claim("oauth_attributes", attributesJson)
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L))
          .signWith(secretKey)
          .compact();
    }

    // 2. SecurityConfig.failureHandler용 임시 토큰 생성
    public String createTempSignupToken(Map<String, Object> attributes, String provider) throws JsonProcessingException {
        // 전달받은 맵(attributes)은 수정 불가능하므로, 새 HashMap으로 복사.
        Map<String, Object> modifiableAttributes = new HashMap<>(attributes);

        // provider 정보를 수정 가능한 맵에 추가
        modifiableAttributes.put("provider", provider);
        String attributesJson = objectMapper.writeValueAsString(modifiableAttributes);

        log.info("createTempSignupToken(Map, provider) call");

        return Jwts.builder()
          .claim("oauth_attributes", attributesJson)
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L))
          .signWith(secretKey)
          .compact();
    }


    // 임시 토큰에서 카카오 사용자 정보를 추출
    public Map<String, Object> getOAuth2AttributesFromTempToken(String token) throws JsonProcessingException {
        String attributesJson = Jwts.parser().verifyWith(secretKey).build()
          .parseSignedClaims(token).getPayload()
          .get("oauth_attributes", String.class);

        return objectMapper.readValue(attributesJson, Map.class);
    }

}