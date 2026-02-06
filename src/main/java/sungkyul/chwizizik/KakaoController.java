package sungkyul.chwizizik;

import sungkyul.chwizizik.dto.KakaoUserInfoResponse;
import sungkyul.chwizizik.service.KakaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/kakao/auth-code") 
    public KakaoUserInfoResponse kakaoLogin(@RequestParam("code") String code) {
        String accessToken = kakaoService.getAccessToken(code);
        return kakaoService.getUserInfo(accessToken);
    }
}