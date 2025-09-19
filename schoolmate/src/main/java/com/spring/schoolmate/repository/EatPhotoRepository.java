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
   * Spring Data JPA의 파생 쿼리(Derived Query)를 사용하여 자동으로 쿼리를 생성
   * @param studentId 학생의 고유 식별자
   * @return 해당 학생이 업로드한 EatPhoto 엔티티 리스트
   */
  List<EatPhoto> findByStudentId(Integer studentId);

  /**
   * 모든 학교의 급식 사진 조회
   * 학생의 역할(role)이 'STUDENT'인 경우만 필터링
   * FETCH JOIN을 사용하여 N+1 문제를 방지, 관련 엔티티(Student, Role)를 한 번에 가져옴.
   * @return 모든 학생(STUDENT)이 업로드한 EatPhoto 엔티티 리스트
   */
  @Query("SELECT p FROM EatPhoto p JOIN FETCH p.student s JOIN FETCH s.role r WHERE r.roleName = 'STUDENT'")
  List<EatPhoto> findAllStudentPhotos();
}