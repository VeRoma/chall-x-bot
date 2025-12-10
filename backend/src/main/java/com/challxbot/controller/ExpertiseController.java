package com.challxbot.controller;

import com.challxbot.domain.Topic;
import com.challxbot.dto.ExpertiseRequest;
import com.challxbot.service.ExpertiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expertise")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*") // Чтобы фронтенд мог стучаться
public class ExpertiseController {

    private final ExpertiseService expertiseService;

    // 1. Получить список доступных предметов
    // GET http://localhost:8080/api/expertise/topics
    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getTopics() {
        return ResponseEntity.ok(expertiseService.getAllTopics());
    }

    // 2. Стать учителем (нужен заголовок X-Telegram-Auth)
    // POST http://localhost:8080/api/expertise/register
    @PostMapping("/register")
    public ResponseEntity<?> becomeExpert(
            @RequestHeader(value = "X-Telegram-Auth", required = false) String authHeader,
            @RequestBody ExpertiseRequest request) {

        // TODO: В будущем здесь будет валидация authHeader и извлечение реального юзера.
        // ПОКА: Хардкодим ID тестового юзера, которого мы создали в DataInitializer (ID=1)
        Long userId = 1L;

        expertiseService.registerAsExpert(userId, request);

        return ResponseEntity.ok(Map.of("message", "Success! You are now an expert."));
    }
}