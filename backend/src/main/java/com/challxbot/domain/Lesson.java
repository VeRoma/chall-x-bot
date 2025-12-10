package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Например: "Глагол to be"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic; // Ссылка на "English Language"

    // ОСНОВНОЙ КОНТЕНТ (Сгенерированный ИИ)
    // Используем TEXT, чтобы влезали большие статьи.
    @Column(columnDefinition = "TEXT")
    private String content;

    // Порядок урока в курсе (1, 2, 3...)
    private int orderIndex;

    // В будущем добавим поле для упражнений:
    // @Column(columnDefinition = "jsonb")
    // private String exercises;
}