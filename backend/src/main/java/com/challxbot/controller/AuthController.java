package com.challxbot.controller;

import com.challxbot.domain.User;
import com.challxbot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userData) {
        // Простейшая авто-регистрация (или вход, если уже есть)
        User user = authService.registerOrLogin(
                userData.getTgId(),
                userData.getUsername(),
                userData.getFirstName()
        );
        return ResponseEntity.ok(user);
    }
}