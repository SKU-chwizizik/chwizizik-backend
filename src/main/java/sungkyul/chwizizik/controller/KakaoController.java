package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sungkyul.chwizizik.config.JwtUtil;
import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.service.KakaoService;
import sungkyul.chwizizik.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/api/kakao/auth-code")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(accessToken);
        User user = userService.saveOrUpdate(userInfo);

        response.addCookie(jwtUtil.createTokenCookie(user.getUserId(), user.getKakaoNickname()));

        return ResponseEntity.ok(Map.of("nickname", user.getKakaoNickname()));
    }

    @GetMapping("/api/kakao/me")
    public ResponseEntity<?> checkLoginStatus(
            @CookieValue(name = "accessToken", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }
        try {
            return ResponseEntity.ok(Map.of("nickname", jwtUtil.getNicknameFromToken(token)));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }
}
