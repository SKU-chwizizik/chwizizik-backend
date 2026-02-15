package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdate(KakaoUserInfoResponse userInfo) {
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();

        return userRepository.findByKakaoId(kakaoId)
                .map(user -> {
                    user.setKakaoNickname(nickname);
                    return user;
                })
                .orElseGet(() -> {
                    return userRepository.save(User.builder()
                            .userId("kakao_" + kakaoId) // ID 필수값 대응
                            .name(nickname)
                            .kakaoId(kakaoId)
                            .kakaoNickname(nickname)
                            .build());
                });
    }
}