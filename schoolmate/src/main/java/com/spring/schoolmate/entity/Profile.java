package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile {

    @Id
    private Long profileId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    // 닉네임
    // NotNull, Unique, Length 제한
    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    // 성별 :: enum으로
    public enum Gender { MALE, FEMALE };
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    // 휴대폰 번호
    @Column(nullable = false, unique = true)
    private String phone;

    // 시도교육청코드
    @Column(name = "atpt_ofcdc_sc_code")
    private String scCode;

    // 학교 행정표준코드
    @Column(name = "sd_schul_code")
    private String schoolCode;

    // 학교 이름
    private String schoolName;

    // 학년
    private short grade;

    private String level;
    private short classNo;
    private String profileImgUrl;


}
