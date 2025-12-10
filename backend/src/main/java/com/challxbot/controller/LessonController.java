package com.challxbot.controller;

import com.challxbot.domain.Lesson;
import com.challxbot.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    // Подключаем репозиторий (Spring сам подставит сюда реализацию)
    private final LessonRepository lessonRepository;

    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<Lesson> getLessonByTopic(@PathVariable Integer topicId) {
        // Теперь метод findByTopicId доступен через переменную lessonRepository
        List<Lesson> lessons = lessonRepository.findByTopicId(topicId);

        if (lessons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Для MVP берем первый попавшийся урок
        return ResponseEntity.ok(lessons.get(0));
    }
}