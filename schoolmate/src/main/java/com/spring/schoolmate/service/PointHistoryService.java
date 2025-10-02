package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.repository.PointHistoryRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp; // Timestamp 사용을 위해 import
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
   * 특정 학생의 전체 포인트 거래 내역을 이메일로 조회합니다. (최신순)
   */
  public List<PointHistory> getHistoryByStudentEmail(String email) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    return pointHistoryRepository.findByStudentOrderByCreatedAtDesc(student);
  }

  /**
   * 새로운 포인트 거래 내역을 기록하고, 학생의 총 포인트 잔액을 업데이트
   */
  @Transactional
  public PointHistory recordTransaction(PointHistory history) {
    Student student = history.getStudent();

    Integer currentBalance = student.getPointBalance();
    Integer newBalance = currentBalance + history.getAmount();

    if (newBalance < 0) {
      throw new IllegalArgumentException("포인트 잔액이 부족하여 거래를 기록할 수 없습니다.");
    }

    history.setBalanceAfter(newBalance);
    student.setPointBalance(newBalance);

    return pointHistoryRepository.save(history);
  }

  /**
   * role이 'STUDENT'인 학생의 이메일을 기반으로 포인트 거래를 기록.
   */
  @Transactional
  public PointHistory recordTransactionByStudentEmail(String email, PointHistory history) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    if (student.getRole() == null || student.getRole().getRoleName() != Role.RoleType.STUDENT) {
      throw new IllegalArgumentException("이메일 " + email + "은(는) STUDENT 역할이 아닙니다.");
    }

    history.setStudent(student);

    return recordTransaction(history);
  }

  /**
   * 학생 ID를 기반으로 포인트를 지급하고 거래 내역을 기록합니다.
   * EatPhotoService에서 호출하는 시그니처를 유지하고, 'reason'은 'refType' 필드에 임시 저장하여 컴파일 오류를 해결합니다.
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
      // ⭐️ 컴파일 오류 해결: 'reason' 필드 대신 기존 엔티티의 'refType' 필드에 거래 사유를 임시 저장합니다. ⭐️
      // 이는 엔티티의 본래 목적과 맞지 않을 수 있지만, 현재 엔티티 구조를 유지하면서 컴파일을 통과하기 위한 조치입니다.
      .refType(reason)
      // tsType은 nullable=false이므로 임의의 값 "EARN"을 할당합니다.
      .tsType("EARN")
      .createdAt(new Timestamp(System.currentTimeMillis()))
      .build();

    // 3. 잔액 업데이트 및 기록 저장
    return recordTransaction(history);
  }
}