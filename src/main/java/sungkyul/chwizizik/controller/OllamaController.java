package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sungkyul.chwizizik.service.OllamaService;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaController {

    private final OllamaService ollamaService;

    @GetMapping("/start")
    public Map<String, String> startInterview(@RequestParam(defaultValue = "basic") String type) {
        return Map.of("question", ollamaService.getInitialQuestion(type));
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> payload) {
        Long interviewId    = Long.valueOf(payload.get("interviewId").toString());
        String userResponse = (String) payload.get("message");
        String lastQuestion = (String) payload.get("lastQuestion");
        String type         = (String) payload.get("type");

        return ollamaService.getNextQuestion(interviewId, userResponse, lastQuestion, type);
    }
}
