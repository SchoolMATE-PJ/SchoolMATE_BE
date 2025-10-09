package com.spring.schoolmate.service;

import org.springframework.data.domain.Page;
import com.spring.schoolmate.dto.admin.AdminProfileUpdateReq;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProfileService {

  private final ProfileRepository profileRepository;
  private final StudentRepository studentRepository;
  private final PointHistoryService pointHistoryService;

  /**
   * 관리자가 특정 학생의 프로필, 학생 정보 및 포인트를 수정합니다.
   *
   * @param studentId 수정할 학생 ID
   * @param req 관리자 수정 요청 DTO
   * @return 수정된 프로필 정보 응답 DTO
   */
  public ProfileRes updateStudentProfile(Long studentId, AdminProfileUpdateReq req) {
    // 1. Profile 조회 (Student와 OneToOne 관계이므로 Student 정보를 가져올 수 있음)
    Profile profile = profileRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("학생 프로필 정보를 찾을 수 없습니다. ID: " + studentId));

    Student student = profile.getStudent();

    // 2. Student 엔티티의 이름 업데이트
    if (req.getName() != null && !req.getName().isEmpty()) {
      student.setName(req.getName());
    }

    // 3. Profile 엔티티의 정보 업데이트
    // Profile 엔티티에 updateByAdmin(AdminProfileUpdateReq req) 메소드가 구현되어야 함.
    profile.updateByAdmin(req);

    // 4. 보유 포인트 업데이트 (Student.pointBalance 기준)
    if (req.getPoints() != null) {
      updateStudentPoints(studentId, req.getPoints());
    }

    // 5. 수정된 정보를 DTO로 변환하여 반환
    return ProfileRes.fromEntity(profile);
  }

  /**
   * 학생의 최종 보유 포인트를 관리자가 지정한 값으로 변경하고, 거래 내역을 기록.
   */
  @Transactional
  private void updateStudentPoints(Long studentId, int targetBalance) {
    Student student = studentRepository.findById(studentId)
      .orElseThrow(() -> new NoSuchElementException("학생 정보를 찾을 수 없습니다."));

    // Student.pointBalance 필드에서 현재 잔액 조회
    int currentBalance = student.getPointBalance();
    int amountDifference = targetBalance - currentBalance; // 변동량 계산

    // 변동량이 0이 아니면 거래 기록
    if (amountDifference != 0) {
      String tsType = amountDifference > 0 ? "ADMIN_GIVE" : "ADMIN_TAKE";
      String refType = "관리자 조정";

      // PointHistoryService를 호출하여 Student.pointBalance 업데이트 및 기록
      // 이 로직은 PointHistoryService에 구현된 recordTransaction(Long studentId, ...) 메소드를 사용합니다.
      pointHistoryService.recordTransaction(
        studentId,
        amountDifference, // 변동량
        null,
        refType,
        tsType
      );
    }
  }

  /**
   * 모든 학생의 프로필 정보를 조회.
   * 학생의 현재 보유 포인트(Student.pointBalance)는 ProfileRes에 포함되어 반환됩니다.
   * @return 모든 학생의 ProfileRes DTO 목록
   */
  @Transactional(readOnly = true)
  public List<ProfileRes> getAllStudentProfiles() {
    // findAll() 시 N+1 문제를 방지하기 위해 JOIN FETCH를 사용하는 Repository 메소드를 사용해야 합니다.
    // 여기서는 findAll()로 가정하고, ProfileRes::fromEntity 변환 시 Student.pointBalance가 함께 로드됩니다.
    List<Profile> profiles = profileRepository.findAll();

    return profiles.stream()
      .map(ProfileRes::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * 페이지네이션 및 검색 조건이 적용된 학생 목록 조회 메서드
   */
  @Transactional(readOnly = true) // 읽기 전용으로 트랜잭션 최적화
  public Page<ProfileRes> getStudentProfiles(
    Pageable pageable,
    String name,
    String phone,
    String school
  ) {
    Page<Student> studentPage;

    // 1. 검색 조건이 있는지 확인 (예시)
    if (name != null && !name.isEmpty()) {
      studentPage = studentRepository.findByNameWithProfile(name, pageable);
    } else if (phone != null && !phone.isEmpty()) {
      studentPage = studentRepository.findByPhoneWithProfile(phone, pageable);
    } else if (school != null && !school.isEmpty()) {
      studentPage = studentRepository.findBySchoolNameWithProfile(school, pageable);
    } else {
      // 2. 검색 조건이 없을 경우: 모든 학생에게 Pageable 적용 (JOIN FETCH 포함)
      studentPage = studentRepository.findAll(pageable);
    }

    // 3. Entity (Student)를 DTO (ProfileRes)로 변환하여 반환
    return studentPage.map(this::convertToProfileRes);
  }

  /**
   * 학생 엔티티(Student)를 응답 DTO(ProfileRes)로 변환하는 헬퍼 메서드
   */
  private ProfileRes convertToProfileRes(Student student) {
    return ProfileRes.builder()
      .studentId(student.getStudentId()) // ID를 no로 사용
      .name(student.getName())
      .schoolName(student.getProfile().getSchoolName())
      .phone(student.getProfile().getPhone())
      .birthDay(student.getProfile().getBirthDay())
      .grade(student.getProfile().getGrade())
      .classNo(student.getProfile().getClassNo())
      .pointBalance(student.getPointBalance())
      .build();
  }
}