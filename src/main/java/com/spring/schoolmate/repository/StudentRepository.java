package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
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

}
