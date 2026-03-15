package sungkyul.chwizizik.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 보안 설정을 활성화합니다.
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
            var config = new org.springframework.web.cors.CorsConfiguration();
            config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
            config.setAllowedHeaders(java.util.List.of("*"));
            config.setAllowCredentials(true); // ★ 이게 있어야 쿠키를 받아줌
            return config;
            }))

            .csrf(csrf -> csrf.disable()) // 1. CSRF 보호 비활성화 (필수)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() 
                .requestMatchers("/signup", "/api/signup").permitAll()
                .requestMatchers("/", "/login/**", "/oauth2/**", "/auth/**", "/kakao/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}