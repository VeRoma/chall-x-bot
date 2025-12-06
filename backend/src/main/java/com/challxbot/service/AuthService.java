package com.challxbot.service;

import com.challxbot.domain.User;
import com.challxbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public User registerOrLogin(Long tgId, String username, String firstName) {
        // Пытаемся найти пользователя по tgId
        return userRepository.findByTgId(tgId)
                .orElseGet(() -> {
                    // Если не нашли, создаем нового (лямбда-выражение выполнится только если Optional пустой)
                    User newUser = User.builder()
                            .tgId(tgId)
                            .username(username)
                            .firstName(firstName)
                            .role("USER")
                            .build();
                    // Сохраняем в базу
                    return userRepository.save(newUser);
                });
    }
}