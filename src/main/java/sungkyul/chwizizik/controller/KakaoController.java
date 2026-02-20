package sungkyul.chwizizik.controller;

import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.service.KakaoService;
import sungkyul.chwizizik.service.UserService;
import sungkyul.chwizizik.config.JwtUtil;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
public class KakaoController {

    private final KakaoService kakaoService;
    private final UserService userService; 
    private final JwtUtil jwtUtil;

    @GetMapping("/kakao/auth-code") 
    public void kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        String accessToken = kakaoService.getAccessToken(code);
        KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(accessToken);
        User user = userService.saveOrUpdate(userInfo);
        
        String jwt = jwtUtil.createToken(user.getUserId(), user.getKakaoNickname());

        Cookie cookie = new Cookie("accessToken", jwt);
        cookie.setHttpOnly((true));
        cookie.setSecure((false));
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);

        response.addCookie(cookie);

        response.sendRedirect("http://localhost:5173/welcome");
    }

    @GetMapping("/api/user/me")
    public ResponseEntity<?> checkLoginStatus(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // 우리가 구워준 'accessToken' 쿠키가 있는지 확인
                if ("accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    String realNickname = jwtUtil.getNicknameFromToken(token);
                    
                    Map<String, String> result = new HashMap<>();
                    result.put("nickname", realNickname); // 실제로는 토큰/DB에서 꺼낸 값
                    return ResponseEntity.ok(result);
                }
            }
        }
        return ResponseEntity.status(401).body("로그인이 필요합니다");
    }

    @PostMapping("/api/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 1. 기존에 발급한 accessToken 쿠키와 이름이 같은 쿠키 생성
        Cookie cookie = new Cookie("accessToken", null);
        
        // 2. 쿠키의 수명을 0으로 설정하여 즉시 삭제되도록 함
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 생성할 때와 동일한 설정 권장

        // 3. 응답에 섞어서 보냄
        response.addCookie(cookie);

        return ResponseEntity.ok().body("로그아웃 성공");
    }
}