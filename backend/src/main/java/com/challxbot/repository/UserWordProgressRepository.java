package com.challxbot.repository;

import com.challxbot.domain.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    // Самый главный метод для Scheduler'а:
    // "Дай мне все записи, где время повторения уже наступило (меньше текущего)"
    @Query("SELECT p FROM UserWordProgress p WHERE p.nextReviewAt <= :now")
    List<UserWordProgress> findAllReadyForReview(LocalDateTime now);

    // Найти прогресс конкретного юзера по слову
    UserWordProgress findByUserIdAndVocabularyId(Integer userId, Long vocabularyId);
}