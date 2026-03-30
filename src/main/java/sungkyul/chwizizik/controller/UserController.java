package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import sungkyul.chwizizik.config.JwtUtil;
import sungkyul.chwizizik.dto.SignupRequest;
import sungkyul.chwizizik.dto.UserProfileResponse;
import sungkyul.chwizizik.service.PortfolioService;
import sungkyul.chwizizik.service.UserService;
import sungkyul.chwizizik.entity.User;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupDto, HttpServletResponse response) {
        try {
            userService.register(signupDto);
            response.addCookie(jwtUtil.createTokenCookie(signupDto.getUserId(), signupDto.getName()));
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignupRequest loginDto, HttpServletResponse response) {
        try {
            User user = userService.login(loginDto.getUserId(), loginDto.getPassword());
            response.addCookie(jwtUtil.createTokenCookie(user.getUserId(), user.getName()));
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
            return ResponseEntity.ok(Map.of("nickname", jwtUtil.getNicknameFromToken(token)));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addCookie(jwtUtil.createExpiredCookie());
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(@CookieValue(name = "accessToken") String token) {
        UserProfileResponse profile = userService.getProfile(jwtUtil.getUserIdFromToken(token));
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/user/profile")
    public ResponseEntity<?> updateProfile(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, String> body) {
        String userId = jwtUtil.getUserIdFromToken(token);
        userService.updateProfile(userId, body.get("name"), body.get("phoneNumber"), body.get("email"), body.get("jobField"));
        return ResponseEntity.ok("수정 완료");
    }

    @PatchMapping("/user/education")
    public ResponseEntity<?> updateEducation(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, String> body) {
        String userId = jwtUtil.getUserIdFromToken(token);
        userService.updateEducation(userId, body.get("level"), body.get("school"), body.get("major"));
        return ResponseEntity.ok("학력 저장 완료");
    }

    @PostMapping("/user/certificates")
    public ResponseEntity<?> addCertificate(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, String> body) {
        userService.addCertificate(jwtUtil.getUserIdFromToken(token), body.get("certName"));
        return ResponseEntity.ok("자격증 등록 완료");
    }

    @DeleteMapping("/user/certificates")
    public ResponseEntity<?> deleteCertificate(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, String> body) {
        userService.deleteCertificate(jwtUtil.getUserIdFromToken(token), body.get("certName"));
        return ResponseEntity.ok("자격증 삭제 완료");
    }

    @PostMapping("/user/files")
    public ResponseEntity<?> uploadResumeFile(
            @CookieValue(name = "accessToken") String token,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("PDF 파일만 업로드 가능합니다.");
        }
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            Map<String, Object> result = portfolioService.saveResumeFile(userId, file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("파일 업로드 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/resumes/{resumeId}")
    public ResponseEntity<?> deleteResume(
            @CookieValue(name = "accessToken") String token,
            @PathVariable Long resumeId) {
        userService.deleteResume(jwtUtil.getUserIdFromToken(token), resumeId);
        return ResponseEntity.ok("이력서 삭제 완료");
    }
}
