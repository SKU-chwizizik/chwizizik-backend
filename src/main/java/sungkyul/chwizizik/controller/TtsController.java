package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                aiServiceUrl + "/tts",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                byte[].class
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(response.getBody());
    }
}
