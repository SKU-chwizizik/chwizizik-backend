package sungkyul.chwizizik.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sungkyul.chwizizik.config.JwtUtil;
import sungkyul.chwizizik.service.RagService;
import sungkyul.chwizizik.service.QuestionPoolService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;
    private final QuestionPoolService questionPoolService;
    private final JwtUtil jwtUtil;

    // frontend type: "executive"→"basic", "technical"→"job"
    private String toBackendType(String frontendType) {
        if ("executive".equalsIgnoreCase(frontendType)) return "basic";
        if ("technical".equalsIgnoreCase(frontendType)) return "job";
        return frontendType; // "basic"/"job" 직접 전달 시 그대로
    }

    @GetMapping("/start")
    public Map<String, Object> startInterview(
            @CookieValue(name = "accessToken") String token,
            @RequestParam(defaultValue = "basic") String type,
            @RequestParam(defaultValue = "ko") String lang) {
        String userId = jwtUtil.getUserIdFromToken(token);
        return ragService.startInterview(userId, toBackendType(type), lang);
    }

    @PostMapping("/sync")
    public Map<String, Object> syncResumes(@RequestParam String userId) {
        return ragService.syncResumes(userId);
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, Object> payload) {
        String userId       = jwtUtil.getUserIdFromToken(token);
        Long interviewId    = Long.valueOf(payload.get("interviewId").toString());
        Long lastQuestionId = payload.get("lastQuestionId") != null
                              ? Long.valueOf(payload.get("lastQuestionId").toString()) : null;
        String userAnswer   = (String) payload.get("message");
        String lastQuestion = (String) payload.get("lastQuestion");
        String type         = toBackendType((String) payload.get("type"));

        return ragService.getNextQuestion(interviewId, lastQuestionId, userId, userAnswer, lastQuestion, type);
    }

    /** 장치 테스트 완료 후: 신규 질문 5개 생성 → 풀 누적 → Interview 생성 */
    @PostMapping("/generate-pool")
    public Map<String, Object> generatePool(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, Object> payload) {
        String userId = jwtUtil.getUserIdFromToken(token);
        String type   = toBackendType((String) payload.get("type"));
        String lang   = payload.getOrDefault("lang", "ko").toString();
        return questionPoolService.generateAndAddToPool(userId, type, lang);
    }

    /** Loading 페이지: 인사말 생성 + 면접 시작 */
    @PostMapping("/start")
    public Map<String, Object> startInterviewNew(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, Object> payload) {
        Long interviewId = Long.valueOf(payload.get("interviewId").toString());
        return questionPoolService.startInterview(interviewId);
    }

    /** 면접 시뮬레이션: 다음 질문 요청 (꼬리 질문 판단 포함) */
    @PostMapping("/next-question")
    public Map<String, Object> nextQuestion(
            @CookieValue(name = "accessToken") String token,
            @RequestBody Map<String, Object> payload) {
        Long interviewId      = Long.valueOf(payload.get("interviewId").toString());
        Long lastQuestionId   = payload.get("lastQuestionId") != null
                                ? Long.valueOf(payload.get("lastQuestionId").toString()) : null;
        String lastAnswer     = (String) payload.get("lastAnswer");
        int mainQuestionOrder = Integer.parseInt(payload.get("mainQuestionOrder").toString());
        List<Long> selectedPoolItemIds = ((List<?>) payload.get("selectedPoolItemIds"))
                .stream().map(id -> Long.valueOf(id.toString())).toList();

        return questionPoolService.getNextQuestion(interviewId, lastQuestionId, lastAnswer,
                mainQuestionOrder, selectedPoolItemIds);
    }
}
