package com.challxbot.repository;

import com.challxbot.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // Этот метод автоматически сгенерирует SQL: SELECT * FROM lessons WHERE topic_id = ?
    List<Lesson> findByTopicId(Integer topicId);
}