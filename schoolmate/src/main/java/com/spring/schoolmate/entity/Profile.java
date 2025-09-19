package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Profile {

    @Id
    @Column(name = "student_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
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

    // 생년월일
    @Column(nullable = false)
    private LocalDate birthDay;

    // 여기서 부터는 Null을 허용하였으나, 회원가입 시에는 필수로 받아야 함

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

    // 교육 수준
    private String level;

    // 반 번호
    private short classNo;

    // 프로필 이미지 URL
    @Column(name = "profile_img_url")
    private String profileImgUrl;


}
