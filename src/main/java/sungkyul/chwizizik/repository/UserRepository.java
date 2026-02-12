package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sungkyul.chwizizik.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
}
