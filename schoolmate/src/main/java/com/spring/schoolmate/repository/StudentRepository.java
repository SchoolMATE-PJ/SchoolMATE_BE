package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.Role;
import com.spring.schoolmate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository  extends JpaRepository<Student, Long> {

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


}
