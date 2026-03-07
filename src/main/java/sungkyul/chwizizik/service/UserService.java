package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.dto.LocalSignupRequest;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
                .orElseGet(() -> {
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
    public User register(LocalSignupRequest dto) {
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
                .phoneNumber(dto.getPhoneNumber())
                .build());
    }
}
