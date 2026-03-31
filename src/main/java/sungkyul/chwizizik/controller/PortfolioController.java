package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sungkyul.chwizizik.config.JwtUtil;
import sungkyul.chwizizik.service.PortfolioService;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> savePortfolio(
            @CookieValue(name = "accessToken") String token,
            @RequestParam String education,
            @RequestParam String school,
            @RequestParam String major,
            @RequestParam String field,
            @RequestParam String cert,
            @RequestParam MultipartFile document) {

        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            portfolioService.savePortfolio(userId, education, school, major, field, cert, document);
            return ResponseEntity.ok("포트폴리오 저장 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("저장 실패: " + e.getMessage());
        }
    }
}
