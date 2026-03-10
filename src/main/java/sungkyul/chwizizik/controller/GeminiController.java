package sungkyul.chwizizik.controller;

import org.springframework.web.bind.annotation.*;
import sungkyul.chwizizik.service.GeminiService;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/start")
    public Map<String, String> startInterview(@RequestParam(defaultValue = "basic") String type) {
        return Map.of("question", geminiService.getInitialQuestion(type));
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> payload) {
        Long interviewId = Long.valueOf(payload.get("interviewId").toString());
        String userResponse = (String) payload.get("message");
        String lastQuestion = (String) payload.get("lastQuestion");
        String type = (String) payload.get("type");

        // 서비스가 주는 결과를 그대로 리액트로 전달함
        return geminiService.getNextQuestion(interviewId, userResponse, lastQuestion, type);
    }
}