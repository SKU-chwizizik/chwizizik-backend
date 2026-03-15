package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import sungkyul.chwizizik.config.JwtUtil;
import sungkyul.chwizizik.dto.SignupRequest;
import sungkyul.chwizizik.service.UserService;
import sungkyul.chwizizik.entity.User;

@RestController
@RequestMapping("/api") // 공통 경로 설정
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // String.valueOf를 사용하면 Null Pointer 경고가 사라집니다.
        Object idAttr = principal.getAttribute("id");
        if (idAttr == null) {
            return ResponseEntity.status(400).body("카카오 아이디 정보를 찾을 수 없습니다.");
        }
        
        String userId = "kakao_" + String.valueOf(idAttr); 

        // DB에서 실제 데이터를 가져옵니다.
        SignupRequest userDto = userService.getUserInfo(userId);

        return ResponseEntity.ok(userDto);
    }

    @PatchMapping
    public ResponseEntity<?> updateUserInfo(@RequestBody SignupRequest dto) {
        userService.updateProfile(dto.getUserId(), dto);
        return ResponseEntity.ok("저장 성공");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupDto, HttpServletResponse response) {
        try {
            userService.register(signupDto); 
            
            // 1. JWT 토큰 생성 (서비스 레이어에서 구현 권장)
            String token = jwtUtil.createToken(signupDto.getUserId(), signupDto.getName());

            // 2. 쿠키에 토큰 담기
            Cookie cookie = new Cookie("accessToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 1일
            response.addCookie(cookie);

            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignupRequest loginDto, HttpServletResponse response) {
        try {
            // 1. DB에서 해당 아이디의 유저 찾기 및 비밀번호 대조
            User user = userService.login(loginDto.getUserId(), loginDto.getPassword());

            // 2. JWT 토큰 생성
            String token = jwtUtil.createToken(user.getUserId(), user.getName());
            
            // 3. 쿠키에 토큰 담기
            Cookie cookie = new Cookie("accessToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 1일
            response.addCookie(cookie);

            return ResponseEntity.ok("로그인 성공");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(name = "accessToken", required = false) String token) {
        if (token == null || token.isEmpty()) {
        return ResponseEntity.status(401).body("로그인이 필요합니다.");
    }
    
    try {
            // JwtUtil을 통해 닉네임 추출
            String nickname = jwtUtil.getNicknameFromToken(token);
            
            Map<String, String> response = new HashMap<>();
            response.put("nickname", nickname); 
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
    // 로그아웃은 클라이언트의 쿠키를 즉시 만료시키는 것이 핵심입니다.
    Cookie cookie = new Cookie("accessToken", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0); // 유효시간을 0으로 설정하여 즉시 삭제
    response.addCookie(cookie);
    
    return ResponseEntity.ok("로그아웃 성공");
}
}