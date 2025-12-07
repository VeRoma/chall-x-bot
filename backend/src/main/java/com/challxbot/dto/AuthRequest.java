package com.challxbot.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private Long tgId;
    private String username;
    private String firstName;

    private String initData; // <--- Строка сырых данных от Telegram для проверки
}