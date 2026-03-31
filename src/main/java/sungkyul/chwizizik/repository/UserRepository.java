package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sungkyul.chwizizik.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUserId(String userId);
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByUserId(String userId);
    Optional<User> findByUserIdOrKakaoId(String userId, Long kakaoId);
}