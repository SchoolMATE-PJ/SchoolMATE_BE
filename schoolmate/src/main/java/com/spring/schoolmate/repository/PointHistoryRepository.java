package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.PointHistory;
import com.spring.schoolmate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 특정 학생의 포인트 거래 내역 조회
    List<PointHistory> findByStudent(Student student);

    // 특정 학생의 포인트 내역을 최신순 정렬해서 가져오기
    List<PointHistory> findByStudentOrderByCreatedAtDesc(Student student);

    // 학생과 트랜잭션 타입(tsType)을 기준으로 개수를 세는 메서드 추가
    Integer countByStudentAndTsType(Student student, String tsType);

    // 특정 학생이 사용(차감)한 포인트의 총합 (amount < 0)
    // SQL: SELECT SUM(ABS(amount)) FROM point_history WHERE student_id = :student AND amount < 0
    @Query("SELECT COALESCE(SUM(ABS(p.amount)), 0) FROM PointHistory p WHERE p.student = :student AND p.amount < 0")
    Integer sumUsedPointsByStudent(@Param("student") Student student);

    // 급식 사진 업로드 수 정확히 카운트
    @Query("SELECT COUNT(p) FROM PointHistory p WHERE p.student = :student AND p.tsType = 'EARN' AND p.refType = '급식 사진 업로드'")
    Integer countMealPhotoUploads(@Param("student") Student student);
}