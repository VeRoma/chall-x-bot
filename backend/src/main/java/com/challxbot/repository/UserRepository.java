package com.challxbot.repository;

import com.challxbot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Метод для поиска пользователя по Telegram ID
    Optional<User> findByTgId(Long tgId);
}