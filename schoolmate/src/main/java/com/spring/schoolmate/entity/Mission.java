package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mission {

    // 미션 아이디
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long mId;

    // 미션 제목
    @Column(name = "mission_title", nullable = false)
    private String mTitle;

    // 미션 상세
    @Column(name = "mission_detail", nullable = false)
    private String mDetail;

    // 미션 성공 시 지급할 포인트 금액
    @Column(name = "deposit_point", nullable = false)
    private Integer mPoint;

    // 시작일
    private LocalDate startDate;

    // 종료일
    private LocalDate endDate;
}
