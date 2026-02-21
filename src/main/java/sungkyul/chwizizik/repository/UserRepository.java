package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sungkyul.chwizizik.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByKakaoId(Long kakaoId);
}