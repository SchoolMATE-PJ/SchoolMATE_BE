// PointHistoryService.java

package com.spring.schoolmate.service;

// 💡 import java.security.Timestamp 삭제 (오류 발생)
// 💡 import java.util.Date 삭제 (사용되지 않음)

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

import java.sql.Timestamp; // 올바른 SQL Timestamp 사용
import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime 추가
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

  // =========================================================================
  // 💡 출석 체크 포인트 지급 및 조회 로직 (중복 제거 및 최종 로직)
  // =========================================================================

  /**
   * 학생 ID를 기반으로 출석 체크 포인트를 지급하고 거래 내역을 기록.
   * (AttendController에서 호출됨)
   *
   * @param studentId 포인트를 지급받을 학생의 ID
   * @return 저장된 PointHistory 객체
   */
  @Transactional
  public PointHistory addAttendancePoint(Long studentId) {
    final int ATTENDANCE_POINT = 500;
    final String REF_TYPE = "출석 체크"; // PointHistory 엔티티의 refType 값
    final String TS_TYPE = "ATTENDANCE"; // 트랜잭션 타입

    // 1. 학생 엔티티 조회
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("ID " + studentId + "에 해당하는 학생을 찾을 수 없습니다."));

    // 2. 출석 중복 체크 (핵심 비즈니스 로직)
    // 오늘 0시 0분 0초 (Local) 이후의 기록을 확인하여 중복 체크
    boolean alreadyAttended = pointHistoryRepository.existsByStudentAndRefTypeAndCreatedAtAfter(
      student,
      REF_TYPE,
      Timestamp.valueOf(LocalDate.now().atStartOfDay())
    );

    if (alreadyAttended) {
      throw new IllegalStateException("이미 오늘 출석 체크를 완료했습니다.");
    }

    // 3. 새로운 PointHistory 엔티티 생성
    PointHistory history = PointHistory.builder()
      .student(student)
      .amount(ATTENDANCE_POINT)
      .refType(REF_TYPE)
      .tsType(TS_TYPE)
      .createdAt(new Timestamp(System.currentTimeMillis()))
      .build();

    // 4. 핵심 로직 호출 (잔액 업데이트 및 DB 기록)
    return recordTransaction(history);
  }

  /**
   * 로그인된 학생의 전체 누적 출석 일수를 조회합니다. (AttendController에서 호출됨)
   * @param email 학생 이메일
   * @return 누적 출석 일수 (Integer)
   */
  public Integer getAttendanceCountByStudentEmail(String email) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NotFoundException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    final String REF_TYPE = "출석 체크"; // PointHistory 엔티티의 refType 값

    // PointHistoryRepository에 해당 학생의 출석 기록 횟수를 조회하는 메서드 필요
    return pointHistoryRepository.countByStudentAndRefType(student, REF_TYPE);
  }

  /**
   * 로그인된 학생의 특정 연/월에 출석한 날짜 리스트를 조회합니다. (AttendController에서 호출됨)
   * @param email 학생 이메일
   * @param year 조회할 연도
   * @param month 조회할 월
   * @return 해당 월에 출석한 날짜 문자열 리스트 (예: ["2025-10-01", "2025-10-02", ...])
   */
  public List<String> getAttendanceDatesByStudentEmailAndMonth(String email, int year, int month) {
    Student student = studentService.findByEmail(email)
      .orElseThrow(() -> new NotFoundException("이메일 " + email + "에 해당하는 학생을 찾을 수 없습니다."));

    final String REF_TYPE = "출석 체크";

    // 1. 해당 월의 시작일과 마지막일 계산
    LocalDate startOfMonth = LocalDate.of(year, month, 1);
    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

    // 해당 월의 시작 시간 (00:00:00)
    Timestamp startTimestamp = Timestamp.valueOf(startOfMonth.atStartOfDay());
    // 해당 월의 마지막 시간 (23:59:59.999...)
    // 또는 다음 달 1일 00:00:00 미만으로 설정하는 것이 DB 쿼리에서 더 명확할 수 있습니다.
    LocalDateTime nextMonthStart = startOfMonth.plusMonths(1).atStartOfDay();
    Timestamp endTimestamp = Timestamp.valueOf(nextMonthStart);

    // 2. PointHistoryRepository에 해당 학생의 특정 기간 출석 기록을 조회하는 메서드 필요
    List<PointHistory> historyList = pointHistoryRepository.findByStudentAndRefTypeAndCreatedAtBetween(
      student,
      REF_TYPE,
      startTimestamp,
      endTimestamp
    );

    // 3. PointHistory 리스트에서 생성 날짜(CreatedAt)만 추출하여 "YYYY-MM-DD" 형식의 문자열로 변환
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return historyList.stream()
      .map(history -> history.getCreatedAt().toLocalDateTime().toLocalDate().format(formatter))
      .distinct() // 중복된 날짜 제거 (하루에 여러 번 출석 기록이 있다면)
      .collect(Collectors.toList());
  }
}