package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.dto.SignupRequest;
import sungkyul.chwizizik.dto.UserProfileResponse;
import sungkyul.chwizizik.entity.Certificates;
import sungkyul.chwizizik.entity.Resume;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.CertificatesRepository;
import sungkyul.chwizizik.repository.ResumeRepository;
import sungkyul.chwizizik.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ResumeRepository resumeRepository;
    private final CertificatesRepository certificatesRepository;

    @Transactional
    public User saveOrUpdate(KakaoUserInfoResponse userInfo) {
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        String generatedUserId = "kakao_" + kakaoId;

        return userRepository.findByUserIdOrKakaoId(generatedUserId, kakaoId)
                .map(user -> {
                    user.setKakaoNickname(nickname);
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .userId(generatedUserId)
                        .name(nickname)
                        .kakaoId(kakaoId)
                        .kakaoNickname(nickname)
                        .build()));
    }

    @Transactional
    public User register(SignupRequest dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        return userRepository.save(User.builder()
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build());
    }

    @Transactional(readOnly = true)
    public User login(String userId, String password) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    // ── 마이페이지 ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserProfileResponse.EducationDto educationDto = null;
        if (user.getEduLevel() != null || user.getSchoolName() != null) {
            educationDto = UserProfileResponse.EducationDto.builder()
                    .level(user.getEduLevel())
                    .school(user.getSchoolName())
                    .major(user.getMajor())
                    .build();
        }

        List<String> certNames = certificatesRepository.findByUser(user).stream()
                .map(Certificates::getCertName)
                .toList();

        List<UserProfileResponse.AttachedFileDto> files = resumeRepository
                .findByUserOrderByUploadedAtDesc(user).stream()
                .map(r -> UserProfileResponse.AttachedFileDto.builder()
                        .id(r.getId())
                        .fileName(r.getFileName())
                        .uploadedAt(r.getUploadedAt())
                        .build())
                .toList();

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .phoneNumber(user.getPhone())
                .email(user.getEmail())
                .jobField(user.getDesiredJob())
                .education(educationDto)
                .certificates(certNames)
                .files(files)
                .build();
    }

    @Transactional
    public void updateProfile(String userId, String name, String phoneNumber, String email, String jobField) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setName(name);
        user.setPhone(phoneNumber);
        user.setEmail(email);
        user.setDesiredJob(jobField);
    }

    @Transactional
    public void updateEducation(String userId, String level, String school, String major) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setEduLevel(level);
        user.setSchoolName(school);
        user.setMajor(major);
    }

    @Transactional
    public void addCertificate(String userId, String certName) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        certificatesRepository.save(Certificates.builder().certName(certName).user(user).build());
    }

    @Transactional
    public void deleteCertificate(String userId, String certName) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        certificatesRepository.deleteByUserAndCertName(user, certName);
    }

    @Transactional
    public void deleteResume(String userId, Long resumeId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));
        if (!resume.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("권한이 없습니다.");
        }
        resumeRepository.delete(resume);
    }
}
