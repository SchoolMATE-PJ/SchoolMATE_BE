package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "allergies")
@Getter
@Setter
@NoArgsConstructor
public class Allergy {

    // 알레르기 ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Integer allergyId;

    // 알레르기명
    @Column(name = "allergy_name", nullable = false, length = 20)
    private String aName;
    
    // 알레르기 번호
    @Column(name = "allergy_no", nullable = false, unique = true)
    private Integer aNo;

}
