package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository  extends JpaRepository<Student, Long> {

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // ID로 특정 학생 검색
    Optional<Student> findByStudentId(Long studentId);

    // Email로 특정 학생 검색
    Optional<Student> findByEmail(String email);

    // 이름으로 회원 목록 찾기
    List<Student> findByName(String name);

    // 특정 권한을 가진 학생
    List<Student> findByRole(Role role);

    // 회원 탈퇴 여부 검색
    List<Student> findByIsDeleted(Boolean isDeleted);

    /**
     * 학생 ID를 기준으로 password와 updatedAt 필드만 업데이트하는 JPQL 쿼리.
     * @param studentId 업데이트할 학생의 ID
     * @param newPassword 암호화된 새 비밀번호
     */
    @Modifying // 이 쿼리가 DB를 수정하는 작업임을 알립니다.
    @Query("UPDATE Student s SET s.password = :newPassword, s.updatedAt = CURRENT_TIMESTAMP WHERE s.studentId = :studentId")
    void updatePassword(@Param("studentId") Long studentId, @Param("newPassword") String newPassword);

    // 기본 findAll() 메서드를 오버라이드하여 Pageable과 JOIN FETCH 적용
    @Query("SELECT s FROM Student s JOIN FETCH s.profile")
    Page<Student> findAll(Pageable pageable);

    // 이름 검색 (JOIN FETCH 적용 및 중복 정의 제거)
    // LIKE 조건에 %를 자동으로 추가하려면 JPQL에서 처리하거나 Service에서 처리.
    // 여기서는 Service에서 받은 name을 사용하고, JPQL에서 LIKE를 처리.
    @Query("SELECT s FROM Student s JOIN FETCH s.profile WHERE s.name LIKE %:name%")
    Page<Student> findByNameWithProfile(@Param("name") String name, Pageable pageable);

    // 연락처 검색 (JOIN FETCH 적용 - phone은 보통 Profile 엔티티에 있음)
    // phone 필드가 Student가 아닌 Profile 엔티티에 있다고 가정하고 수정
    @Query("SELECT s FROM Student s JOIN FETCH s.profile p WHERE p.phone LIKE %:phone%")
    Page<Student> findByPhoneWithProfile(@Param("phone") String phone, Pageable pageable);

    // 학교명 검색 (JOIN FETCH 적용)
    // schoolName 필드가 Profile 엔티티에 있다고 가정하고 수정
    @Query("SELECT s FROM Student s JOIN FETCH s.profile p WHERE p.schoolName LIKE %:schoolName%")
    Page<Student> findBySchoolNameWithProfile(@Param("schoolName") String schoolName, Pageable pageable);
}
