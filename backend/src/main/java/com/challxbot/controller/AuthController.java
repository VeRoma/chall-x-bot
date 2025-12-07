package com.challxbot.controller;

import com.challxbot.domain.User;
import com.challxbot.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*") // Можно раскомментировать, если будут проблемы с CORS
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public User loginOrRegister(@RequestBody User userData) {
        // 1. Ищем пользователя в базе по Telegram ID
        Optional<User> existingUser = userRepository.findByTgId(userData.getTgId());

        if (existingUser.isPresent()) {
            // Если нашли — возвращаем его
            return existingUser.get();
        } else {
            // 2. Если не нашли — СОЗДАЕМ нового (Регистрация)
            User newUser = new User();
            newUser.setTgId(userData.getTgId());
            newUser.setUsername(userData.getUsername());
            newUser.setFirstName(userData.getFirstName());
            // Если у вас есть поле role, установите дефолтную
            newUser.setRole("USER");
            newUser.setCreatedAt(LocalDateTime.now());

            // Сохраняем в базу
            return userRepository.save(newUser);
        }
    }
}