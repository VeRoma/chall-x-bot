package com.challxbot.repository;

import com.challxbot.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Поиск урока по названию
    Optional<Lesson> findByTitle(String title);

    // Поиск уроков по ID темы.
    // ВАЖНО: Убедитесь, что в классе Lesson есть поле topicId или связь с Topic.
    // Если у вас связь @ManyToOne private Topic topic, то метод лучше назвать findByTopic_Id(Integer id)
    List<Lesson> findByTopicId(Integer topicId);
}