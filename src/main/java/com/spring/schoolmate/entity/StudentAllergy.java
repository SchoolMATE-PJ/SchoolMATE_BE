package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_allergies")
@Getter
@NoArgsConstructor
public class StudentAllergy {

    // StudentAllergy 생성자 직접 추가함 이렇게만 넣도록
    public StudentAllergy(Student student, Allergy allergy) {
        this.student = student;
        this.allergy = allergy;
    }

    // 학생 보유 알레르기 고유 아이디
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sa_id")
    private Integer saId;

    // student_id :: Student Entity PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // allergy_id :: Allergy Entity PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergy allergy;
}
