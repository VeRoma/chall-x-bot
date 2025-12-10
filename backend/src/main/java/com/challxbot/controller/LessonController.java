package com.challxbot.repository;

import com.challxbot.domain.Lesson; // Или com.challxbot.model.Lesson (проверьте, где у вас лежит сущность Lesson)
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Это отдельный интерфейс, он НЕ внутри контроллера
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByTitle(String title);

    // Также нам нужен метод поиска по topicId, который вы вызываете в контроллере
    // Если поле в Lesson называется topicId:
    List<Lesson> findByTopic_Id(Integer topicId);

    // Если в Lesson есть связь @ManyToOne private Topic topic, то метод будет:
    // List<Lesson> findByTopic_Id(Integer topicId);
}