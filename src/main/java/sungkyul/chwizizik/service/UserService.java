package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.dto.SignupRequest;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User saveOrUpdate(KakaoUserInfoResponse userInfo) { // 소셜 로그인 시 유저 정보 저장 또는 업데이트
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        String generatedUserId = "kakao_" + kakaoId; // 카카오 ID를 기반으로 고유한 userId 생성
        
        return userRepository.findByUserIdOrKakaoId(generatedUserId, kakaoId) // userId 또는 kakaoId로 기존 유저 조회
                .map(user -> { // 기존 유저가 있으면 정보 업데이트
                    user.setKakaoNickname(nickname); 
                    return user;
                })
                .orElseGet(() -> { // 기존 유저가 없으면 새로 생성
                    return userRepository.save(User.builder()
                            .userId(generatedUserId)
                            .name(nickname)
                            .kakaoId(kakaoId)
                            .kakaoNickname(nickname)
                            // .provider("KAKAO")
                            .build());
                });
    }

    @Transactional
    public User register(SignupRequest dto) { // 일반 회원 가입
        // 중복 아이디 체크
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 일반 회원 저장
        return userRepository.save(User.builder()
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build());
    }

    @Transactional
    public User updateProfile(String userId, SignupRequest dto) { // 회원 정보 업데이트   
        // 1. 기존 유저 찾기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 정보 업데이트
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone()); // DTO 필드명 확인 필요
        return user;
    }

    @Transactional
    public SignupRequest getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // DB에서 가져온 엔티티를 프론트로 보낼 DTO로 변환합니다.
        SignupRequest dto = new SignupRequest();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone()); // DB 필드명에 맞춰 매칭
        dto.setEmail(user.getEmail());
        
        return dto;
    }

    @Transactional(readOnly = true) // 로그인 조회는 읽기 전용으로 설정하는 것이 효율적입니다.
    public User login(String userId, String password) {
        // 1. 아이디로 유저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        // 2. 비밀번호 일치 여부 확인
        // passwordEncoder.matches(평문 비번, 암호화된 비번)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public User getUserInfoEntity(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
}
}
