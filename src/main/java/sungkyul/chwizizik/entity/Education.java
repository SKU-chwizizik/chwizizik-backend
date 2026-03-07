package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "edu_level", length = 50)
    private String eduLevel; // 최종학력

    @Column(name = "school_name", length = 100)
    private String schoolName; // 학교명

    @Column(name = "major", length = 100)
    private String major; // 전공

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
