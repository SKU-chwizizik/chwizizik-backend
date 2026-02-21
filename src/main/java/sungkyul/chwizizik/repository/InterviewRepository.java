package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sungkyul.chwizizik.entity.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}