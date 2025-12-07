package com.challxbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramValidationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    public boolean isDataSafe(String initData) {
        if (initData == null || initData.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> params = parseInitData(initData);
            String receivedHash = params.remove("hash");
            if (receivedHash == null) return false;

            // Сортируем параметры по алфавиту key=value
            String dataCheckString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));

            // Вычисляем секретный ключ (HMAC-SHA-256 от токена с ключом "WebAppData")
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] secret = sha256_HMAC.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            // Подписываем нашу строку полученным секретом
            SecretKeySpec dataKey = new SecretKeySpec(secret, "HmacSHA256");
            sha256_HMAC.init(dataKey);
            byte[] hashBytes = sha256_HMAC.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            // Переводим в hex и сравниваем
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(receivedHash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, String> parseInitData(String initData) {
        return Arrays.stream(initData.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        p -> decode(p[0]),
                        p -> decode(p.length > 1 ? p[1] : "")
                ));
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }
}