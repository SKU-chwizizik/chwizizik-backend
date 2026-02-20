package sungkyul.chwizizik.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secretKey = "your_very_secret_key_should_be_long_enough_to_be_safe_12345678";
    private final long expirationTime = 1000 * 60 * 60 * 24; // 유효기간 24시간

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성기
    public String createToken(String userId, String nickname) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("nickname", nickname);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 닉네임만 꺼내기
    public String getNicknameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("nickname");
    }
}