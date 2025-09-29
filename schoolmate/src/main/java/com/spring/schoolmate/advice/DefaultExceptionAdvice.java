package com.spring.schoolmate.advice;

import com.spring.schoolmate.exception.DMLException;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.exception.UserAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class DefaultExceptionAdvice {

  /**
   * UserAuthenticationException 처리 핸들러
   * - 인증 및 권한 관련 예외 처리 (e.g., 로그인 실패, JWT 만료)
   * - 예외 자체에 HTTP 상태 코드가 포함되어 있으므로 이를 사용.
   */
  @ExceptionHandler(UserAuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleUserAuthenticationException(UserAuthenticationException e) {
    log.warn("인증/권한 예외 발생: {}", e.getMessage());

    Map<String, String> errorBody = new HashMap<>();
    errorBody.put("title", e.getTitle());
    errorBody.put("message", e.getMessage());

    // 예외 객체에 저장된 HttpStatus를 사용하여 응답.
    return new ResponseEntity<>(errorBody, e.getHttpStatus());
  }

  /**
   * NotFoundException 처리 핸들러
   * - 요청한 리소스를 찾을 수 없을 때 (e.g., 특정 ID의 사용자 없음)
   * - HTTP 상태 코드: 404 Not Found
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e) {
    log.warn("요청 리소스 찾을 수 없음 예외 발생: {}", e.getMessage());

    Map<String, String> errorBody = new HashMap<>();
    errorBody.put("title", "Resource Not Found");
    errorBody.put("message", e.getMessage());

    return new ResponseEntity<>(errorBody, org.springframework.http.HttpStatus.NOT_FOUND);
  }

  /**
   * DMLException 처리 핸들러
   * - 데이터베이스 DML(Insert, Update, Delete) 작업 중 오류 발생 시
   * - HTTP 상태 코드: 500 Internal Server Error (데이터 처리 실패)
   */
  @ExceptionHandler(DMLException.class)
  public ResponseEntity<Map<String, String>> handleDMLException(DMLException e) {
    log.error("데이터베이스 DML 예외 발생: {}", e.getMessage());

    Map<String, String> errorBody = new HashMap<>();
    errorBody.put("title", "Database Operation Failed");
    errorBody.put("message", "데이터 처리 중 오류가 발생했습니다: " + e.getMessage());

    return new ResponseEntity<>(errorBody, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 일반적인 모든 RuntimeException 처리 핸들러 (최후의 수단)
   * - 정의된 핸들러 외의 모든 예측하지 못한 런타임 예외를 처리.
   * - HTTP 상태 코드: 500 Internal Server Error
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
    log.error("예상치 못한 런타임 예외 발생: {}", e.getMessage(), e);

    Map<String, String> errorBody = new HashMap<>();
    errorBody.put("title", "Internal Server Error");
    errorBody.put("message", "서버 처리 중 예상치 못한 오류가 발생했습니다.");

    return new ResponseEntity<>(errorBody, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
  }
}