package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    // 알레르기 ID
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Integer allergyId;

    // 알레르기명
    @Column(name = "allergy_name", nullable = false, length = 20)
    private String allergyName;
    
    // 알레르기 번호
    @Column(name = "allergy_no", nullable = false, unique = true)
    private Integer allergyNo;

}
