package com.challxbot.controller;

import com.challxbot.domain.User;
import com.challxbot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // Автоматически создает конструктор для final полей
@Slf4j
public class AuthController {

    private final AuthService authService;

    // Принимаем объект User, так как фронтенд пока шлет его (а не AuthRequest)
    // В будущем заменим на AuthRequest для безопасности
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userData) {
        log.info("🔑 Запрос на вход: tgId={}", userData.getTgId());

        User user = authService.registerOrLogin(
                userData.getTgId(),
                userData.getUsername(),
                userData.getFirstName()
        );

        return ResponseEntity.ok(user);
    }
}