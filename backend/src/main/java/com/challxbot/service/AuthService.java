package com.challxbot.service;

import com.challxbot.domain.User;
import com.challxbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public User registerOrLogin(Long tgId, String username, String firstName) {
        return userRepository.findByTgId(tgId)
                .orElseGet(() -> {
                    log.info("👤 Регистрирую нового пользователя: {}", username);

                    User newUser = User.builder()
                            .tgId(tgId)
                            .username(username)
                            .firstName(firstName)
                            .role("USER")
                            .starsBalance(0L) // <--- ФИКС ОШИБКИ БД (NOT NULL)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return userRepository.save(newUser);
                });
    }
}