package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.pointhistory.PointHistoryReq; // ⭐️ 관리자 지급 DTO
import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.exception.NotFoundException; // Controller에서 사용할 수 있도록 유지
import com.spring.schoolmate.repository.PointHistoryRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

  private final PointHistoryRepository pointHistoryRepository;
  private final StudentService studentService;
  private final StudentRepository studentRepository;

  /**
   * 특정 학생의 현재 보유 포인트를 이메일로 조회.
   * Controller의 /student/{email}/balance 엔드포인트에 사용.
   * @param email 학생 이메일
   * @return 현재 포인트 잔액 (Integer)
   * @throws NoSuchElementException 학생을 찾을 수 없을 때 (Controller에서 404 처리)
   */
  public Integer getCurrentBalanceByStudentEmail(String email) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    return student.getPointBalance();
  }

  /**
   * 특정 학생의 전체 포인트 거래 내역을 이메일로 조회. (최신순)
   * Controller의 /student/{email} GET 요청에 사용.
   */
  public List<PointHistory> getHistoryByStudentEmail(String email) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    return pointHistoryRepository.findByStudentOrderByCreatedAtDesc(student);
  }

  /**
   * 새로운 포인트 거래 내역을 기록하고, 학생의 총 포인트 잔액을 업데이트.
   * 모든 트랜잭션 기록의 핵심 로직.
   */
  @Transactional
  public PointHistory recordTransaction(PointHistory history) {
    Student student = history.getStudent();

    Integer currentBalance = student.getPointBalance();
    Integer newBalance = currentBalance + history.getAmount();

    if (newBalance < 0) {
      // 잔액 부족 오류는 Controller에서 400 BAD_REQUEST로 처리.
      throw new IllegalArgumentException("포인트 잔액이 부족하여 거래를 기록할 수 없습니다. 필요한 포인트: " + (-history.getAmount()));
    }

    // history 엔티티에 최종 잔액을 기록
    history.setBalanceAfter(newBalance);

    // Student 엔티티의 잔액을 업데이트하고 저장 (잔액 변경 사항 커밋)
    student.setPointBalance(newBalance);
    studentRepository.save(student);

    // PointHistory 엔티티 저장
    return pointHistoryRepository.save(history);
  }

  /**
   * role이 'STUDENT'인 학생의 이메일을 기반으로 포인트 거래를 기록. (상품 교환 등)
   * Controller의 /student/{email} POST 요청에 사용.
   */
  @Transactional
  public PointHistory recordTransactionByStudentEmail(String email, PointHistory history) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // STUDENT 역할 검증
    if (student.getRole() == null || student.getRole().getRoleName() != Role.RoleType.STUDENT) {
      throw new IllegalArgumentException("이메일 " + email + "은(는) STUDENT 역할이 아니므로 거래를 기록할 수 없습니다.");
    }

    history.setStudent(student);
    history.setCreatedAt(new Timestamp(System.currentTimeMillis()));

    return recordTransaction(history);
  }

  /**
   * 학생 ID를 기반으로 포인트를 지급하고 거래 내역을 기록.
   * (EatPhotoService와 같은 다른 서비스에서 호출하는 용도)
   */
  @Transactional
  public PointHistory addPointTransaction(Long studentId, int amount, String reason) {
    // 1. 학생 엔티티 조회
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("ID " + studentId + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. 새로운 PointHistory 엔티티 생성
    PointHistory history = PointHistory.builder()
      .student(student)
      .amount(amount)
      // 기존 로직 유지: 'reason'을 'refType'에 저장 (EatPhotoService와의 호환성 유지)
      .refType(reason)
      .tsType("EARN")
      .createdAt(new Timestamp(System.currentTimeMillis()))
      .build();

    // 3. 잔액 업데이트 및 기록 저장
    return recordTransaction(history);
  }
}