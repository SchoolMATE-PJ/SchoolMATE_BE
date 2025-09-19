package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 특정 학생의 포인트 거래 내역 조회
    List<PointHistory> findByStudent(Student student);

    // 특정 학생의 포인트 내역을 최신순 정렬해서 가져오기
    List<PointHistory> findByStudentOrderByCreatedAtDesc(Student student);

}
