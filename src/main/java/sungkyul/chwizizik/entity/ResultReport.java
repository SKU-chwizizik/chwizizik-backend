package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "result_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    // 연관관계 설정 - 결과 리포트는 하나의 인터뷰에 속하므로 1:1 관계로 설정한거임
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // 카테고리별 점수 JSON: {"커뮤니케이션": 78, "논리적사고": 65, ...}
    @Lob
    @Column(name = "softskill_analysis", columnDefinition = "TEXT")
    private String softskillAnalysis;

    // GENERATING | READY
    @Column(name = "status", length = 20)
    private String status;

    @Lob
    @Column(name = "voice_analysis", columnDefinition = "TEXT")
    private String voiceAnalysis;

    @Lob
    @Column(name = "nonverbal_analysis", columnDefinition = "TEXT")
    private String nonverbalAnalysis;
}