package com.challxbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // HTML урока

    @Column(columnDefinition = "TEXT")
    private String quizJson; // JSON теста (храним как строку)

    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonIgnore
    private Topic topic;

    // Полезный конструктор
    public Lesson(String title, String content, String quizJson, Topic topic, Integer orderIndex) {
        this.title = title;
        this.content = content;
        this.quizJson = quizJson;
        this.topic = topic;
        this.orderIndex = orderIndex;
    }
}