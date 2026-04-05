package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sungkyul.chwizizik.entity.*;
import sungkyul.chwizizik.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionPoolService {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;
    private final QuestionPoolItemRepository questionPoolItemRepository;

    /**
     * 신규 질문 5개 생성 → 풀에 추가 → Interview 생성 (POOL_READY 상태)
     * @return interviewId
     */
    @Transactional
    public Map<String, Object> generateAndAddToPool(String userId, String type, String lang) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // Interview 생성 (POOL_GENERATING)
        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .interviewType(type)
                .language(lang)
                .interviewAt(LocalDateTime.now())
                .status("POOL_GENERATING")
                .build());

        // FastAPI /generate-pool 호출
        Map<String, Object> requestBody = Map.of("user_id", userId, "interview_type", type);
        List<Map<String, String>> questions = new ArrayList<>();
        try {
            Map<?, ?> response = restTemplate.postForObject(aiServiceUrl + "/generate-pool", requestBody, Map.class);
            if (response != null && response.get("questions") instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> q) {
                        questions.add(Map.of(
                            "category", String.valueOf(q.get("category")),
                            "text", String.valueOf(q.get("text"))
                        ));
                    }
                }
            }
        } catch (Exception e) {
            interview.setStatus("POOL_GENERATING");
            interviewRepository.save(interview);
            throw new RuntimeException("질문 풀 생성 실패: " + e.getMessage());
        }

        // QuestionPoolItem 저장 (user + interviewType 귀속)
        for (Map<String, String> q : questions) {
            questionPoolItemRepository.save(QuestionPoolItem.builder()
                    .user(user)
                    .interviewType(type)
                    .category(q.get("category"))
                    .questionText(q.get("text"))
                    .useCount(0)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // 풀 준비 완료
        interview.setStatus("POOL_READY");
        interviewRepository.save(interview);

        long totalPoolSize = questionPoolItemRepository.findByUserAndInterviewType(user, type).size();

        return Map.of(
            "interviewId", interview.getInterviewId(),
            "addedCount", questions.size(),
            "totalPoolSize", totalPoolSize,
            "status", "POOL_READY"
        );
    }

    /**
     * 누적 풀에서 5개 선택 (A 3개 + B 2개).
     * POOR 질문 60% 확률 재등장 → 미출제 우선 → GOOD 오래된 순
     */
    public List<QuestionPoolItem> selectQuestionsForInterview(User user, String type) {
        Random random = new Random();

        List<QuestionPoolItem> selected = new ArrayList<>();
        // A 3개, B 2개
        Map<String, Integer> categorySlots = Map.of("A", 3, "B", 2);

        for (Map.Entry<String, Integer> entry : categorySlots.entrySet()) {
            String category = entry.getKey();
            int slots = entry.getValue();

            List<QuestionPoolItem> all = questionPoolItemRepository
                    .findByUserAndInterviewTypeAndCategory(user, type, category);

            // Step 1: POOR 질문 60% 확률 판정
            List<QuestionPoolItem> poorCandidates = all.stream()
                    .filter(q -> "POOR".equals(q.getLastAnswerQuality()))
                    .filter(q -> random.nextDouble() < 0.6)
                    .collect(Collectors.toList());

            // Step 2: 미출제 질문 (useCount = 0)
            List<QuestionPoolItem> unasked = all.stream()
                    .filter(q -> q.getUseCount() == 0)
                    .collect(Collectors.toList());

            // Step 3: GOOD이고 오래된 순 (lastUsedAt 기준)
            List<QuestionPoolItem> goodOld = all.stream()
                    .filter(q -> "GOOD".equals(q.getLastAnswerQuality()))
                    .sorted(Comparator.comparing(
                            q -> q.getLastUsedAt() != null ? q.getLastUsedAt() : LocalDateTime.MIN))
                    .collect(Collectors.toList());

            // 슬롯 채우기: POOR → 미출제 → GOOD 오래된 순
            Set<Long> pickedIds = new HashSet<>();
            List<QuestionPoolItem> categorySelected = new ArrayList<>();

            for (QuestionPoolItem item : poorCandidates) {
                if (categorySelected.size() >= slots) break;
                if (pickedIds.add(item.getPoolItemId())) categorySelected.add(item);
            }
            for (QuestionPoolItem item : unasked) {
                if (categorySelected.size() >= slots) break;
                if (pickedIds.add(item.getPoolItemId())) categorySelected.add(item);
            }
            for (QuestionPoolItem item : goodOld) {
                if (categorySelected.size() >= slots) break;
                if (pickedIds.add(item.getPoolItemId())) categorySelected.add(item);
            }

            selected.addAll(categorySelected);
        }

        return selected;
    }

    /**
     * 면접 시작: 풀에서 5개 선택 → 인터뷰에 저장 → greeting 반환
     */
    @Transactional
    public Map<String, Object> startInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다: " + interviewId));

        if (!"POOL_READY".equals(interview.getStatus())) {
            throw new IllegalStateException("질문 풀이 준비되지 않았습니다. 현재 상태: " + interview.getStatus());
        }

        // 풀에서 5개 선택 후 Interview에 연결 저장
        List<QuestionPoolItem> selected = selectQuestionsForInterview(
                interview.getUser(), interview.getInterviewType());

        // 선택된 질문을 면접 세션용 Question으로 미리 저장 (questionOrder 1~5 부여)
        // 실제 출제는 getNextQuestion에서 순서대로 진행
        // 여기서는 선택 결과를 Interview의 메타데이터로 활용 (세션 내 순서 보장용)
        // 선택된 풀 아이템 ID를 interview에 별도 저장하는 대신,
        // useCount를 0에서 pending(-1) 처리 없이 getNextQuestion에서 관리
        // → 선택된 아이템 목록을 반환만 하고 실제 Question 저장은 getNextQuestion에서 수행

        // 인사말 하드코딩
        String greeting = "job".equals(interview.getInterviewType())
            ? "안녕하세요, 개발팀 김 팀장입니다. 오늘 기술 면접에 참여해 주셔서 감사합니다. 긴장하지 마시고 본인의 역량을 충분히 발휘해 주시길 바랍니다. 그럼 시작하겠습니다."
            : "안녕하세요, 박부장입니다. 오늘 면접에 와 주셔서 반갑습니다. 편안한 마음으로 자연스럽게 이야기 나눠 봅시다. 그럼 시작하겠습니다.";

        interview.setStatus("IN_PROGRESS");
        interviewRepository.save(interview);

        // 선택된 풀 아이템 ID 목록을 반환 (프론트→백 세션 유지용 or 백에서 순서 관리)
        List<Long> selectedPoolItemIds = selected.stream()
                .map(QuestionPoolItem::getPoolItemId)
                .collect(Collectors.toList());

        return Map.of(
            "greeting", greeting,
            "interviewId", interviewId,
            "selectedPoolItemIds", selectedPoolItemIds
        );
    }

    /**
     * 다음 질문 반환. 꼬리 질문 여부 판단 → 풀에서 순서대로 선택.
     * mainQuestionOrder: 현재 진행할 본 질문 순서 (1~5)
     */
    @Transactional
    public Map<String, Object> getNextQuestion(
            Long interviewId, Long lastQuestionId, String lastAnswer,
            int mainQuestionOrder, List<Long> selectedPoolItemIds) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다: " + interviewId));

        String type = interview.getInterviewType();
        String userId = interview.getUser().getUserId();

        // 1. 이전 답변 저장
        Question lastQuestion = null;
        if (lastQuestionId != null && lastAnswer != null) {
            lastQuestion = questionRepository.findById(lastQuestionId).orElse(null);
            if (lastQuestion != null) {
                lastQuestion.setAnswerText(lastAnswer);
                questionRepository.save(lastQuestion);
            }
        }

        // 2. 꼬리 질문 가능 여부 확인 (전체 2개 이하, 해당 질문 1개 이하)
        boolean canFollowup = false;
        if (lastQuestion != null && lastQuestion.getParentQuestion() == null) {
            int totalFollowups = questionRepository.countByInterviewAndParentQuestionIsNotNull(interview);
            int thisQuestionFollowups = questionRepository.countByParentQuestion(lastQuestion);
            canFollowup = totalFollowups < 2 && thisQuestionFollowups < 1;
        }

        // 3. 꼬리 질문 판단 (FastAPI /should-followup)
        if (canFollowup && lastQuestion != null) {
            Map<String, Object> followupCheckBody = Map.of(
                "interview_type", type,
                "question_text", lastQuestion.getQuestionText(),
                "user_answer", lastAnswer != null ? lastAnswer : "",
                "current_followup_count", questionRepository.countByInterviewAndParentQuestionIsNotNull(interview)
            );
            try {
                Map<?, ?> resp = restTemplate.postForObject(aiServiceUrl + "/should-followup", followupCheckBody, Map.class);
                if (resp != null && Boolean.TRUE.equals(resp.get("should_followup"))) {
                    // 꼬리 질문 생성
                    Map<String, Object> followupBody = Map.of(
                        "interview_type", type,
                        "parent_question", lastQuestion.getQuestionText(),
                        "user_answer", lastAnswer != null ? lastAnswer : ""
                    );
                    Map<?, ?> followupResp = restTemplate.postForObject(aiServiceUrl + "/generate-followup", followupBody, Map.class);
                    String followupText = followupResp != null ? String.valueOf(followupResp.get("followup_question")) : "";

                    int followupIndex = questionRepository.countByParentQuestion(lastQuestion) + 1;
                    Question followupQ = questionRepository.save(Question.builder()
                            .interview(interview)
                            .questionText(followupText)
                            .parentQuestion(lastQuestion)
                            .followupIndex(followupIndex)
                            .createdAt(LocalDateTime.now())
                            .build());

                    return Map.of(
                        "question", followupText,
                        "questionId", followupQ.getQuestionId(),
                        "isFollowUp", true,
                        "isFinished", false
                    );
                }
            } catch (Exception ignored) {}
        }

        // 4. 5번 본 질문 모두 완료 → 마무리 멘트
        if (mainQuestionOrder > 5) {
            String closing = "job".equals(type)
                ? "오늘 기술 면접에 참여해 주셔서 감사합니다. 결과는 추후 별도로 안내드리겠습니다. 수고하셨습니다."
                : "오늘 면접에 와 주셔서 정말 감사합니다. 좋은 결과가 있기를 바라겠습니다. 수고하셨습니다.";

            interview.setStatus("COMPLETED");
            interviewRepository.save(interview);

            // 면접 완료 후 답변 평가 비동기 처리 (별도 메서드로 분리)
            evaluateAnswersAsync(interview, userId, type);

            return Map.of("question", closing, "isFollowUp", false, "isFinished", true);
        }

        // 5. 풀에서 다음 본 질문 선택 (선택된 풀 아이템 순서대로)
        // selectedPoolItemIds 인덱스: A0,A1,A2,B0,B1 순 → mainQuestionOrder 1~5 매핑
        // Q1→idx0, Q2→idx1, ... Q5→idx4
        int idx = mainQuestionOrder - 1;
        if (idx >= selectedPoolItemIds.size()) {
            throw new IllegalStateException("선택된 질문 풀 범위를 초과했습니다.");
        }

        Long poolItemId = selectedPoolItemIds.get(idx);
        QuestionPoolItem poolItem = questionPoolItemRepository.findById(poolItemId)
                .orElseThrow(() -> new IllegalArgumentException("풀 아이템을 찾을 수 없습니다: " + poolItemId));

        // 출제 기록 갱신
        poolItem.setUseCount(poolItem.getUseCount() + 1);
        poolItem.setLastUsedAt(LocalDateTime.now());
        questionPoolItemRepository.save(poolItem);

        Question question = questionRepository.save(Question.builder()
                .interview(interview)
                .questionText(poolItem.getQuestionText())
                .questionOrder(mainQuestionOrder)
                .poolItem(poolItem)
                .createdAt(LocalDateTime.now())
                .build());

        return Map.of(
            "question", poolItem.getQuestionText(),
            "questionId", question.getQuestionId(),
            "questionOrder", mainQuestionOrder,
            "isFollowUp", false,
            "isFinished", false
        );
    }

    /**
     * 면접 완료 후 각 본 질문-답변 쌍 평가 → poolItem.lastAnswerQuality 업데이트
     */
    private void evaluateAnswersAsync(Interview interview, String userId, String type) {
        // 본 질문(questionOrder != null)만 평가
        List<Question> mainQuestions = interview.getQuestions().stream()
                .filter(q -> q.getQuestionOrder() != null && q.getAnswerText() != null)
                .collect(Collectors.toList());

        for (Question q : mainQuestions) {
            if (q.getPoolItem() == null) continue;
            try {
                Map<String, Object> evalBody = Map.of(
                    "interview_type", type,
                    "question_text", q.getQuestionText(),
                    "user_answer", q.getAnswerText()
                );
                Map<?, ?> resp = restTemplate.postForObject(aiServiceUrl + "/evaluate-answer", evalBody, Map.class);
                if (resp != null && resp.get("quality") instanceof String quality) {
                    q.getPoolItem().setLastAnswerQuality(quality);
                    questionPoolItemRepository.save(q.getPoolItem());
                }
            } catch (Exception ignored) {}
        }
    }
}
