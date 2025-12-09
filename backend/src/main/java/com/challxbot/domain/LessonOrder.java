package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    private Long amount; // Сумма в звездах

    // Статусы: 0=CREATED, 1=PAID, 2=COMPLETED, 3=CANCELED
    @Column(nullable = false)
    private byte status;

    private LocalDateTime scheduledFor;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}