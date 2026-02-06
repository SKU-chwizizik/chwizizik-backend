package sungkyul.chwizizik;

import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.service.KakaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin; // 1. 이거 추가
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // 2. 이거 추가 (모든 사람 접속 허용)
public class KakaoController {

    private final KakaoService kakaoService;

    // 아까 성공했던 그 주소 그대로 둡니다!
    @GetMapping("/kakao/auth-code") 
    public KakaoUserInfoResponse kakaoLogin(@RequestParam("code") String code) {
        String accessToken = kakaoService.getAccessToken(code);
        return kakaoService.getUserInfo(accessToken);
    }
}