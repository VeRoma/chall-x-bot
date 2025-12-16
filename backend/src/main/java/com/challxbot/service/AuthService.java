package com.challxbot.service;

import com.challxbot.domain.User;
import com.challxbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
// Remove class-level @Transactional or strictly control it
// import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    // REMOVED @Transactional here to allow catching the exception without breaking the caller's transaction status
    // or forcing a global rollback that prevents subsequent reads.
    // The repository methods (save, findByTgId) are already transactional by default.
    public User registerOrLogin(Long tgId, String username, String firstName) {
        // 1. Try to find existing user
        Optional<User> existingUser = userRepository.findByTgId(tgId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // 2. If not found, create new
        log.info("👤 Регистрирую нового пользователя: {}", username);
        User newUser = User.builder()
                .tgId(tgId)
                .username(username)
                .firstName(firstName)
                .role("USER")
                .starsBalance(0L)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            // 3. Try to save. If race condition happens, this throws Exception
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // 4. Handle race condition: if insert failed because user was created in parallel
            log.warn("⚠️ Гонка потоков для пользователя {}. Загружаю из БД.", tgId);

            // Now we can safely read from DB because the previous failed transaction (inside save) is finished.
            return userRepository.findByTgId(tgId)
                    .orElseThrow(() -> new RuntimeException("Неожиданная ошибка: пользователь не найден после сбоя вставки"));
        } catch (Exception e) {
            // Catch potential Hibernate assertion failures if they bubble up differently
            log.error("❌ Ошибка при создании пользователя", e);
            return userRepository.findByTgId(tgId)
                    .orElseThrow(() -> new RuntimeException("Ошибка регистрации"));
        }
    }
}