package sungkyul.chwizizik.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "kakao_nickname", length = 100)
    private String kakaoNickname;

    @Column(name = "kakao_profile", length = 500)
    private String kakaoProfile;
}
