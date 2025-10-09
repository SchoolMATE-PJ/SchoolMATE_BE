package com.spring.schoolmate.entity;

import com.spring.schoolmate.dto.profile.ProfileUpdateReq;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    // Profile student_id :: Student PK + FK
    @Id
    @Column(name = "student_id")
    private Long studentId;

    // Student와 Profile의 Entity는 1:1 관계
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
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    // 생년월일
    @Column(name = "birthday", nullable = false)
    private LocalDate birthDay;

    // 여기서 부터는 Null을 허용하였으나, 회원가입 시에는 필수로 받아야 함

    // 시도교육청코드
    @Column(name = "atpt_ofcdc_sc_code")
    private String scCode;

    // 학교 행정표준코드
    @Column(name = "sd_schul_code")
    private String schoolCode;

    // 학교 이름
    @Column(name = "school_name")
    private String schoolName;
    
    // 학과명
    @Column(name = "major_name")
    private String majorName;

    // 학년
    @Column(name = "grade")
    private Integer grade;

    // 교육 수준 ex) 초등학교, 중학교, 고등학교
    @Column(name = "level")
    private String level;

    // 반 번호
    @Column(name = "class_no")
    private Integer classNo;

    // 프로필 이미지 URL
    @Column(name = "profile_img_url")
    private String profileImgUrl;

    // DTO를 받아 프로필 정보를 업데이트하는 메소드
    /**
     * ProfileUpdateReq DTO를 받아 프로필 정보를 업데이트합니다.
     * JPA의 '더티 체킹'에 의해 이 메소드가 호출되고 트랜잭션이 끝나면 자동으로 DB에 반영됩니다.
     */
    public void update(ProfileUpdateReq req) {
        if (req.getNickname() != null) this.nickname = req.getNickname();
        if (req.getGender() != null) this.gender = req.getGender();
        if (req.getPhone() != null) this.phone = req.getPhone();
        if (req.getBirthDay() != null) this.birthDay = req.getBirthDay();
        if (req.getProfileImgUrl() != null) this.profileImgUrl = req.getProfileImgUrl();
        if (req.getScCode() != null) this.scCode = req.getScCode();
        if (req.getSchoolCode() != null) this.schoolCode = req.getSchoolCode();
        if (req.getSchoolName() != null) this.schoolName = req.getSchoolName();
        if (req.getMajorName() != null) this.majorName = req.getMajorName();
        if (req.getGrade() != null) this.grade = req.getGrade();
        if (req.getLevel() != null) this.level = req.getLevel();
        if (req.getClassNo() != null) this.classNo = req.getClassNo();
    }

    /**
     * 프로필 이미지 URL을 업데이트.
     * @param imageUrl 새로 업로드된 URL 또는 삭제 시 null
     */
    public void updateProfileImageUrl(String imageUrl) {
        this.profileImgUrl = imageUrl;
    }
}