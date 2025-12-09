package com.challxbot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_id", unique = true, nullable = false)
    private Long tgId;

    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Builder.Default
    private String role = "USER";

    // Кошелек пользователя (звезды)
    @Builder.Default
    @Column(nullable = false)
    private Long starsBalance = 0L;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}