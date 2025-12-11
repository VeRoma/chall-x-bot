package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vocabulary")
@Data
@NoArgsConstructor
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word; // Само слово (например, "Spring")

    @Column(nullable = false)
    private String translationShort; // Для кнопки (например, "Весна")

    @Column(columnDefinition = "TEXT")
    private String translationFull; // Полная справка (например, "1. Весна 2. Пружина...")

    @Column(nullable = false)
    private Integer rank; // Частотность (1 = самое популярное)

    private String partOfSpeech; // noun, verb, adjective...

    @Column(columnDefinition = "TEXT")
    private String traps; // JSON массив ловушек: ["Струна", "Кольцо", "Петь"]

    // Конструктор для удобства
    public Vocabulary(String word, String translationShort, String translationFull, Integer rank, String partOfSpeech, String traps) {
        this.word = word;
        this.translationShort = translationShort;
        this.translationFull = translationFull;
        this.rank = rank;
        this.partOfSpeech = partOfSpeech;
        this.traps = traps;
    }
}