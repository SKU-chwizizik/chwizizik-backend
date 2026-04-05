package sungkyul.chwizizik.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({JwtException.class, MissingRequestCookieException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthException(Exception e, HttpServletResponse response) {
        // 만료/무효 토큰이면 쿠키도 만료시킴
        Cookie expired = new Cookie("accessToken", null);
        expired.setHttpOnly(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        response.addCookie(expired);
        return Map.of("error", "인증이 만료되었습니다. 다시 로그인해주세요.");
    }
}
