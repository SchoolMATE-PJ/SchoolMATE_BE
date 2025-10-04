package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.pointhistory.PointHistoryReq;
import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.exception.NotFoundException;
import com.spring.schoolmate.repository.PointHistoryRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
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
   */
  public Integer getCurrentBalanceByStudentEmail(String email) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    return student.getPointBalance();
  }

  /**
   * 특정 학생의 전체 포인트 거래 내역을 이메일로 조회. (최신순)
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
      // 🚨 잔액 부족 오류 발생 시 메시지를 포함하여 throw
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
   * 학생 ID를 기반으로 포인트 거래를 기록. (ProductExchangeService에서 호출하는 용도)
   */
  @Transactional
  public void recordTransaction(
    Long studentId,           // 학생 ID
    Integer amount,           // 포인트 변동량 (차감 시 음수)
    Long refId,               // 참조 ID
    String refType,           // 참조 타입 (예: PRODUCT)
    String transactionType    // 트랜잭션 타입 (예: EXCHANGE)
  ) {
    // 1. 학생 찾기 (잔액 업데이트용)
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("학생을 찾을 수 없습니다."));

    // 2. 잔액 업데이트
    int newBalance = student.getPointBalance() + amount;
    if (newBalance < 0) {
      // 이 오류는 ProductExchangeService에서 이미 걸러졌지만, 안전을 위해 유지
      throw new IllegalArgumentException("잔액 부족");
    }
    student.setPointBalance(newBalance);
    studentRepository.save(student);

    // 3. PointHistory 엔티티 생성 및 저장
    PointHistory history = new PointHistory();
    history.setStudent(student);
    history.setAmount(amount);
    history.setRefId(refId);
    history.setRefType(refType);
    history.setTsType(transactionType);
    history.setBalanceAfter(newBalance); // 업데이트된 잔액 기록

    history.setCreatedAt(new Timestamp(System.currentTimeMillis()));

    pointHistoryRepository.save(history);
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

  /**
   * 학생의 이메일을 기반으로 포인트 거래를 기록. (관리자 지급/차감 등 일반 거래 기록)
   * 🚨 PointHistoryController에서 호출하는 메서드입니다.
   */
  @Transactional
  public PointHistory recordTransactionByStudentEmail(String email, PointHistory history) {
    // 1. 이메일로 학생 찾기
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. PointHistory 엔티티에 Student 객체 설정
    history.setStudent(student);

    // 3. 기록 시각 설정 (엔티티에 @CreationTimestamp가 있지만, 확실하게 설정)
    if (history.getCreatedAt() == null) {
      history.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }

    // 4. 핵심 로직 호출 (잔액 업데이트 및 DB 기록)
    // 이 메서드는 잔액 부족 검증 및 Student/PointHistory 저장을 수행합니다.
    return recordTransaction(history);
  }

  /**
   * 특정 학생의 급식 사진 업로드 거래 횟수를 이메일로 조회.
   * (Controller의 GET /api/point-history/student/me/meal-count 에서 호출됩니다.)
   */
  public Integer getMealPhotoUploadCountByStudentEmail(String email) {
    // 1. 이메일로 학생 찾기
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. 💡 수정된 로직: refType='급식 사진 업로드' and tsType='EARN' 인 내역 횟수 조회
    return pointHistoryRepository.countMealPhotoUploads(student);
  }

  /**
   * 특정 학생이 사용한 포인트의 총합을 조회.
   * (Controller의 GET /api/point-history/student/me/used-points 에서 호출됩니다.)
   * @param email 학생 이메일
   * @return 사용한 포인트 총합 (amount < 0 인 내역의 절댓값 합산)
   */
  public Integer getSumOfUsedPointsByStudentEmail(String email) {
    // 1. 이메일로 학생 찾기
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NoSuchElementException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. Repository를 통해 사용된 포인트 총합 조회
    return pointHistoryRepository.sumUsedPointsByStudent(student);
  }
}