package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_word_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"}) // Защита от дублей
})
@Data
@NoArgsConstructor
public class UserWordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    private Integer level = 0; // Уровень знания (0..5)

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt; // Когда спросить в следующий раз

    // Дополнительные поля статистики (опционально)
    private Integer correctAnswers = 0;
    private Integer wrongAnswers = 0;

    public UserWordProgress(User user, Vocabulary vocabulary) {
        this.user = user;
        this.vocabulary = vocabulary;
        this.level = 0;
        this.nextReviewAt = LocalDateTime.now(); // Готов к изучению сразу
    }
}