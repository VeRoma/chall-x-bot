package com.challxbot.controller;

import com.challxbot.domain.Lesson;
import com.challxbot.repository.LessonRepository;
import com.challxbot.repository.TopicRepository; // <-- Важный импорт
import com.challxbot.service.GeminiService;     // <-- Важный импорт
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository; // Добавляем репозиторий тем
    private final GeminiService geminiService;     // Добавляем сервис AI

    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<Lesson> getLessonByTopic(@PathVariable Integer topicId) {
        List<Lesson> lessons = lessonRepository.findByTopicId(topicId);
        if (lessons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lessons.get(0));
    }

    // 👇 ЭТОТ МЕТОД ОТСУТСТВОВАЛ
    @GetMapping("/by-topic/{topicId}/quiz")
    public ResponseEntity<?> getQuizByTopic(@PathVariable Integer topicId) {
        return topicRepository.findById(topicId)
                .map(topic -> {
                    // Генерируем квиз
                    String quizContent = geminiService.generateQuiz(topic.getName());
                    // Возвращаем JSON
                    return ResponseEntity.ok(Map.of("content", quizContent));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}