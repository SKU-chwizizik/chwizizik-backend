package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sungkyul.chwizizik.service.RagService;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @GetMapping("/start")
    public Map<String, String> startInterview(@RequestParam(defaultValue = "basic") String type) {
        return Map.of("question", ragService.getInitialQuestion(type));
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> payload) {
        Long interviewId    = Long.valueOf(payload.get("interviewId").toString());
        String userId       = (String) payload.get("userId");
        String userAnswer   = (String) payload.get("message");
        String lastQuestion = (String) payload.get("lastQuestion");
        String type         = (String) payload.get("type");

        return ragService.getNextQuestion(interviewId, userId, userAnswer, lastQuestion, type);
    }
}
