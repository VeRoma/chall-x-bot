package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Исправлено: Short -> Integer для совместимости с Postgres SERIAL

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;
}