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
    @Column(name = "summary")
    private String summary;

    @Column(name = "softskill_score")
    private Integer softskillScore;

    @Lob
    @Column(name = "voice_analysis")
    private String voiceAnalysis;

    @Lob
    @Column(name = "nonverbal_analysis")
    private String nonverbalAnalysis;
}