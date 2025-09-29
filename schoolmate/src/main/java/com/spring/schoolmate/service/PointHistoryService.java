package com.spring.schoolmate.service;

import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role; // Role 엔티티 import 추가
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

  private final PointHistoryRepository pointHistoryRepository;
  private final StudentService studentService;

  /**
   * 특정 학생의 전체 포인트 거래 내역을 최신순으로 조회
   * @param email 조회할 학생의 email
   * @return PointHistory 객체 리스트 (최신순)
   */
  public List<PointHistory> getHistoryByStudentId(String email) {
    // 1. 학생 엔티티 조회
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NotFoundException("해당하는 학생을 찾지 못했습니다."));

    // 2. Repository 메서드 호출
    return pointHistoryRepository.findByStudentOrderByCreatedAtDesc(student);
  }

  /**
   * 특정 학생의 전체 포인트 거래 내역을 이메일로 조회합니다. (최신순)
   * @param email 조회할 학생의 이메일
   * @return PointHistory 객체 리스트 (최신순)
   */
  public List<PointHistory> getHistoryByStudentEmail(String email) {
    // 1. 학생 엔티티 조회
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. Repository 메서드 호출
    return pointHistoryRepository.findByStudentOrderByCreatedAtDesc(student);
  }

  /**
   * 새로운 포인트 거래 내역을 기록하고, 학생의 총 포인트 잔액을 업데이트
   * @param history 기록할 PointHistory 엔티티
   * @return 저장된 PointHistory 엔티티
   */
  @Transactional // 데이터 변경(쓰기) 작업
  public PointHistory recordTransaction(PointHistory history) {
    Student student = history.getStudent();

    // 1. 현재 잔액 확인 및 새로운 잔액 계산
    Integer currentBalance = student.getPointBalance();
    Integer newBalance = currentBalance + history.getAmount();

    if (newBalance < 0) {
      throw new IllegalArgumentException("포인트 잔액이 부족하여 거래를 기록할 수 없습니다.");
    }

    // 2. PointHistory에 최종 잔액 (balanceAfter) 설정
    history.setBalanceAfter(newBalance);

    // 3. Student 엔티티의 포인트 잔액 업데이트
    student.setPointBalance(newBalance);
    // JPA의 더티 체킹에 의해 student 엔티티는 트랜잭션 종료 시 자동 업데이트.

    // 4. PointHistory 기록 저장
    return pointHistoryRepository.save(history);
  }

  /**
   * role이 'STUDENT'인 학생의 이메일을 기반으로 포인트 거래를 기록.
   * @param email 거래를 기록할 학생의 이메일
   * @param history 기록할 PointHistory 엔티티 (amount, tsType 등이 포함되어야 함)
   * @return 저장된 PointHistory 엔티티
   */
  @Transactional
  public PointHistory recordTransactionByStudentEmail(String email, PointHistory history) {
    // 1. 이메일로 학생 엔티티 조회 및 검증
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. 역할(Role)이 STUDENT인지 확인
    if (student.getRole() == null || student.getRole().getRoleName() != Role.RoleType.STUDENT) {
      throw new IllegalArgumentException("이메일 " + email + "은(는) STUDENT 역할이 아닙니다.");
    }

    // 3. PointHistory 엔티티에 조회된 Student 설정
    history.setStudent(student);

    // 4. 기존 recordTransaction 로직을 호출하여 잔액 업데이트 및 기록 저장
    return recordTransaction(history);
  }
}