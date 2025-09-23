package com.spring.schoolmate.repository;

import com.spring.schoolmate.entity.EatPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EatPhotoRepository extends JpaRepository<EatPhoto, Integer> {

  /**
   * 특정 학생이 업로드한 모든 급식 사진 조회
   * @param studentId 학생의 고유 식별자
   * @return 해당 학생이 업로드한 EatPhoto 엔티티 리스트
   */
  List<EatPhoto> findByStudent_StudentId(Integer studentId);

  /**
   * 모든 학교의 급식 사진 조회
   * 학생의 역할(role)이 'STUDENT'인 경우만 필터링
   * @return 모든 학생(STUDENT)이 업로드한 EatPhoto 엔티티 리스트
   */
  @Query("SELECT p FROM EatPhoto p JOIN FETCH p.student s JOIN FETCH s.role r WHERE r.roleName = 'STUDENT'")
  List<EatPhoto> findAllStudentPhotos();
}