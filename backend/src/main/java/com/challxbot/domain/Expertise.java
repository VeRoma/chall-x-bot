package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expertise", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "topic_id"}) // Защита от дублей
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // Темы легкие, можно грузить сразу
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // ОПТИМИЗАЦИЯ:
    private Integer pricePerHour; // Звезды (int достаточно)

    // Рейтинг 0-5. В Java - byte (1 байт), в Postgres мапится на smallint.
    @Column(columnDefinition = "SMALLINT DEFAULT 0")
    private byte rating;

    @Builder.Default
    private boolean isExpertEnabled = false; // По умолчанию отключено, пока не заполнит профиль
}