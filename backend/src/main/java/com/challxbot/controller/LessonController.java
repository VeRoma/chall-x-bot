package com.challxbot.controller;

import com.challxbot.domain.Lesson;
import com.challxbot.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
public class LessonController {

    private final LessonRepository lessonRepository;
    private final Random random = new Random();

    // Получить урок (Случайный вариант!)
    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<Lesson> getLessonByTopic(@PathVariable Integer topicId) {
        // 1. Достаем ВСЕ варианты уроков по этой теме
        List<Lesson> lessons = lessonRepository.findByTopicId(topicId);

        if (lessons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 2. Выбираем случайный
        Lesson randomLesson = lessons.get(random.nextInt(lessons.size()));

        log.info("🎲 Пользователю выпал урок ID: {} (из {} вариантов)", randomLesson.getId(), lessons.size());

        return ResponseEntity.ok(randomLesson);
    }

    // Этот метод теперь нужен только если вы хотите получить квиз отдельно,
    // но вообще он теперь приходит внутри объекта Lesson в поле quizJson.
    // Оставим для совместимости с текущим фронтендом, но логику поменяем.
    @GetMapping("/by-topic/{topicId}/quiz")
    public ResponseEntity<?> getQuizByTopic(@PathVariable Integer topicId) {
        // Если фронт запрашивает квиз отдельно, лучше найти тот же урок, который был показан.
        // Но так как у нас нет сессии урока, для простоты вернем случайный квиз из той же темы.
        // В ИДЕАЛЕ: Фронтенд должен брать quizJson прямо из ответа /by-topic/{topicId}

        List<Lesson> lessons = lessonRepository.findByTopicId(topicId);
        if (lessons.isEmpty()) return ResponseEntity.notFound().build();

        Lesson randomLesson = lessons.get(random.nextInt(lessons.size()));
        return ResponseEntity.ok(Map.of("content", randomLesson.getQuizJson()));
    }
}