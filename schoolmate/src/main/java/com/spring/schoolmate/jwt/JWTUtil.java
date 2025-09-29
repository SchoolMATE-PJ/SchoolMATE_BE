package com.spring.schoolmate.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final SecretKey secretKey;//Decode한 secret key를 담는 객체
    private final Long expirationTime;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 처리를 위해 추가

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

    /**
     * 신규 소셜 회원을 위한 '임시 회원가입용 토큰'을 생성합니다.
     * 이 토큰은 유효시간이 짧으며, 카카오로부터 받은 원본 사용자 정보를 담고 있습니다.
     *
     * @param oAuth2User 카카오로부터 받은 사용자 정보 객체
     * @return 임시 토큰 문자열
     */
    public String createTempSignupToken(OAuth2CustomUser oAuth2User) throws JsonProcessingException {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String attributesJson = objectMapper.writeValueAsString(attributes);

        return Jwts.builder()
                .claim("oauth_attributes", attributesJson) // 카카오에서 받은 정보 전체를 저장
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L)) // 유효시간 10분
                .signWith(secretKey)
                .compact();
    }

    /**
     * 임시 토큰을 검증하고, 그 안에 저장된 카카오 사용자 정보를 추출합니다.
     *
     * @param token 프론트엔드로부터 받은 임시 토큰
     * @return 카카오 사용자 정보가 담긴 Map
     */
    public Map<String, Object> getOAuth2AttributesFromTempToken(String token) throws JsonProcessingException {
        String attributesJson = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("oauth_attributes", String.class);

        return objectMapper.readValue(attributesJson, Map.class);
    }

}