package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.schoolmate.entity.Admin; // 🚨 [추가] Admin 엔티티 임포트
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

    // ======================== 검증 메서드 확장 ========================

    // 🚨 [수정] getStudentId 대신, 'ID'를 추출하는 공통 메서드 추가 (Student/Admin 모두 처리)
    public Long getId(String token) {
        String role = getRole(token);
        String idClaimName = "STUDENT".equals(role) ? "studentId" : "adminId"; // claim 이름 분리 (호환성 유지)

        log.info("getId(String token) :: call");
        Long rId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(idClaimName, Long.class);
        log.info("getId(String token) id = {}", rId);
        return rId;
    }

    // 🚨 [삭제] getStudentId 삭제 (getId로 통합)
    /*
    public Long getStudentId(String token) { ... }
    */

    // 검증 Email (변경 없음)
    public String getEmail(String token) {
        log.info("getEmail(String token) :: call");
        String rEmail = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
        log.info("getEmail(String token)  re = {}" ,rEmail);
        return rEmail;
    }

    // 🚨 [수정] getStudentName 대신, 'Name'을 추출하는 공통 메서드 추가
    public String getName(String token) {
        log.info("getName(String token)  call");
        // JWT 생성 시 Student는 studentName, Admin은 name을 사용한다고 가정하고 분기 처리
        String rName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("studentName", String.class);
        if (rName == null) {
            rName = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
        }
        log.info("getName(String token)  rName = {}" ,rName);
        return rName;
    }

    // 🚨 [삭제] getStudentName 삭제 (getName으로 통합)
    /*
    public String getStudentName(String token) { ... }
    */


    // 검증 Role (변경 없음)
    public String getRole(String token) {
        log.info("getRole(String token) :: call");
        String rRole = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
        log.info("getRole(String token) role = {}", rRole);
        return rRole;
    }

    // 검증 Expired (변경 없음)
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


    // ======================== 생성 메서드 확장 ========================

    // Student용 JWT 생성 (변경 없음, claim key는 studentId, studentName 유지)
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

    // 🚨 [추가] Admin용 JWT 생성
    public String createJwt(Admin admin) {
        log.info("createJwt(Admin) call");
        return Jwts.builder()
          .claim("adminId", admin.getAdminId()) // 🚨 claim key를 adminId로 설정
          .claim("email", admin.getEmail())
          .claim("role", admin.getRole().toString()) // RoleType이 아닌 Role Enum 자체를 쓴다고 가정
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + expirationTime))
          .signWith(secretKey)
          .compact();
    }


    // 신규 소셜 회원을 위한 '임시 회원가입용 토큰' 생성 (변경 없음)
    public String createTempSignupToken(OAuth2CustomUser oAuth2User) throws JsonProcessingException {
        // ... (로직 유지)
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String attributesJson = objectMapper.writeValueAsString(attributes);

        return Jwts.builder()
          .claim("oauth_attributes", attributesJson) // 카카오에서 받은 정보 전체를 저장
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L)) // 유효시간 10분
          .signWith(secretKey)
          .compact();
    }

    // 임시 토큰에서 카카오 사용자 정보를 추출 (변경 없음)
    public Map<String, Object> getOAuth2AttributesFromTempToken(String token) throws JsonProcessingException {
        // ... (로직 유지)
        String attributesJson = Jwts.parser().verifyWith(secretKey).build()
          .parseSignedClaims(token).getPayload()
          .get("oauth_attributes", String.class);

        return objectMapper.readValue(attributesJson, Map.class);
    }

}