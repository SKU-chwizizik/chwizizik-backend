package sungkyul.chwizizik.service;

import sungkyul.chwizizik.dto.KakaoTokenResponse;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class KakaoService {

    private final UserRepository userRepository;

    public KakaoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        RestTemplate rt = new RestTemplate();

        ResponseEntity<KakaoTokenResponse> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                KakaoTokenResponse.class
        );

        return response.getBody().getAccessToken();
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> request = new HttpEntity<>(headers);

        RestTemplate rt = new RestTemplate();

        ResponseEntity<KakaoUserInfoResponse> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                KakaoUserInfoResponse.class
        );

        KakaoUserInfoResponse kakaoUser = response.getBody();


        saveOrUpdateUser(kakaoUser);

        return kakaoUser;
    }

    private void saveOrUpdateUser(KakaoUserInfoResponse kakaoUser) {

        if (kakaoUser == null || kakaoUser.getId() == null) return;

        Long kakaoId = kakaoUser.getId();

        String nickname = null;
        String profileImageUrl = null;

        if (kakaoUser.getKakaoAccount() != null &&
            kakaoUser.getKakaoAccount().getProfile() != null) {

            nickname = kakaoUser.getKakaoAccount().getProfile().getNickname();
            profileImageUrl =
                    kakaoUser.getKakaoAccount().getProfile().getProfileImageUrl();
        }

        String userId = "kakao_" + kakaoId;

        User user = userRepository.findById(userId)
                .orElseGet(() ->
                        User.builder()
                                .userId(userId)
                                .build()
                );

        user.setName(nickname);
        user.setProfileImage(profileImageUrl);

        userRepository.save(user);
    }
}
