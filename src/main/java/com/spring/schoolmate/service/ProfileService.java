package com.spring.schoolmate.service;

import com.spring.schoolmate.dto.profile.MyProfileRes;
import com.spring.schoolmate.dto.profile.ProfileReq;
import com.spring.schoolmate.dto.profile.ProfileRes;
import com.spring.schoolmate.dto.profile.ProfileUpdateReq;
import com.spring.schoolmate.entity.Profile;
import com.spring.schoolmate.entity.Student;
import com.spring.schoolmate.repository.ProfileRepository;
import com.spring.schoolmate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final StudentRepository studentRepository;
    private final AllergyService allergyService;
    private final FirebaseStorageService storageService;

    /**
     * 프로필 정보를 저장
     * @param student 프로필과 연관될 학생 엔티티
     * @param profileReq 프로필 정보 DTO
     * @return 저장된 프로필 엔티티
     */
    @Transactional
    public Profile registerProfile(Student student, ProfileReq profileReq) {
        Profile newProfile = Profile.builder()
                .student(student) // 엔티티 직접 참조
                .nickname(profileReq.getNickname())
                .gender(profileReq.getGender())
                .phone(profileReq.getPhone())
                .birthDay(profileReq.getBirthDay())
                .scCode(profileReq.getScCode())
                .schoolCode(profileReq.getSchoolCode())
                .schoolName(profileReq.getSchoolName())
                .majorName(profileReq.getMajorName())
                .grade(profileReq.getGrade())
                .classNo(profileReq.getClassNo())
                .level(profileReq.getLevel())
                .profileImgUrl(profileReq.getProfileImgUrl())
                .build();
        return profileRepository.save(newProfile);
    }

    /**
     * 닉네임과 전화번호 중복을 검사합니다.
     * @param profileReq 프로필 정보 DTO
     */
    public void duplicateCheck(ProfileReq profileReq) {
        if (profileRepository.existsByNickname(profileReq.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (profileRepository.existsByPhone(profileReq.getPhone())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
    }

    // 내 프로필 정보 조회 메소드
    /**
     * 로그인한 사용자의 ID로 프로필 정보를 조회합니다.
     * @param studentId (JWT 토큰에서 추출한) 사용자 ID
     * @return 프로필 정보 응답 DTO
     */
    @Transactional
    public ProfileRes getProfile(Long studentId) {
        Profile profile = profileRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("프로필 정보를 찾을 수 없습니다."));

        // Entity를 Response DTO로 변환하여 반환
        return ProfileRes.fromEntity(profile);
    }

    /**
     * '내 정보' 페이지를 위한 모든 정보 조회 (Student 정보 포함)
     * @param studentId 사용자 ID
     * @return MyProfileRes (이름, 이메일, 알레르기 등 모든 정보)
     */
    @Transactional(readOnly = true)
    public MyProfileRes getMyProfileDetails(Long studentId) {
        Profile profile = profileRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("프로필 정보를 찾을 수 없습니다."));
        // 새로운 통합 DTO인 MyProfileRes를 반환
        return MyProfileRes.fromEntity(profile);
    }

    /**
     * 로그인한 사용자의 프로필 정보를 수정합니다.
     * @param studentId (JWT 토큰에서 추출한) 사용자 ID
     * @param req 수정할 전체 정보가 담긴 요청 DTO
     * @return 수정된 프로필 정보 응답 DTO
     */
    @Transactional
    public MyProfileRes updateProfile(Long studentId, ProfileUpdateReq req) {
        Profile profile = profileRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("프로필 정보를 찾을 수 없습니다. ID: " + studentId));

        // 닉네임 중복 검사 (새 닉네임이 기존과 다를 경우에만)
        if (req.getNickname() != null && !req.getNickname().equals(profile.getNickname())) {
            if (profileRepository.existsByNickname(req.getNickname())) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
        }

        // 전화번호 중복 검사 (새 전화번호가 기존과 다를 경우에만)
        if (req.getPhone() != null && !req.getPhone().equals(profile.getPhone())) {
            if (profileRepository.existsByPhone(req.getPhone())) {
                throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
            }
        }

        // Student 엔티티의 이름(name) 업데이트 ---
        if (req.getName() != null) {
            Student student = profile.getStudent();
            student.setName(req.getName());
        }

        // Profile 엔티티에 만들어 둔 update 메소드를 호출하여 정보 변경
        profile.update(req);

        // 알레르기 정보 업데이트
        if (req.getAllergyId() != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다..."));
            allergyService.updateStudentAllergies(student, req.getAllergyId());
        }

        // 수정된 정보를 DTO로 변환하여 반환 (JPA의 더티 체킹에 의해 자동 저장됨)
        return MyProfileRes.fromEntity(profile);
    }

    // 프로필 이미지 업로드 및 DB 업데이트 메서드
    // -----------------------------------------------------------
    @Transactional
    public String uploadProfileImage(Long studentId, MultipartFile file) {
        // 1. 해당 학생의 프로필 정보 로드
        Profile profile = profileRepository.findByStudentId(studentId)
          .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다."));

        // 2. 파일 스토리지에 업로드하고 URL 받기 (FirebaseStorageService 사용)
        //    * 경로는 'profiles/{studentId}' 등으로 지정하는 것이 일반적입니다.
        String imageUrl;
        try {
            imageUrl = storageService.uploadFile(file, "profiles/" + studentId);
        } catch (Exception e) {
            // 업로드 실패 시 예외 처리
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }

        // 3. Profile 엔티티의 profileImgUrl 업데이트
        profile.updateProfileImageUrl(imageUrl);
        // @Transactional 때문에 변경 사항이 자동 저장되지만, 명시적으로 저장할 수도 있습니다.
        // profileRepository.save(profile);

        return imageUrl; // 클라이언트에게 저장된 URL 반환
    }

    // -----------------------------------------------------------
    // ⭐️ [신규] 프로필 이미지 URL 삭제 및 DB 업데이트 메서드
    // -----------------------------------------------------------
    @Transactional
    public void deleteProfileImage(Long studentId) {
        Profile profile = profileRepository.findByStudentId(studentId)
          .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다."));

        // 1. (선택) Firebase/S3에서 실제 파일도 삭제하는 로직 추가 가능
        //    storageService.deleteFile(profile.getProfileImgUrl());

        // 2. Profile 엔티티의 profileImgUrl을 null로 업데이트 (기본 이미지로 돌아감)
        profile.updateProfileImageUrl(null);
    }
}