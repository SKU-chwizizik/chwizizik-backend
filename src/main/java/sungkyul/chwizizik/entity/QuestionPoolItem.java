package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_pool_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class QuestionPoolItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pool_item_id")
    private Long poolItemId;

    // 풀은 Interview가 아닌 User + interviewType 단위로 귀속
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "interview_type", length = 50, nullable = false)
    private String interviewType;  // "basic" | "job"

    @Column(name = "category", length = 1, nullable = false)
    private String category;  // "A" | "B"

    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Builder.Default
    @Column(name = "use_count", nullable = false)
    private Integer useCount = 0;  // 출제 횟수 (0 = 미출제)

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;  // 마지막 출제 시각 (null = 미출제)

    // "GOOD" | "POOR" | null (미출제)
    // 면접 종료 후 AI 답변 평가 시 업데이트
    @Column(name = "last_answer_quality", length = 10)
    private String lastAnswerQuality;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
