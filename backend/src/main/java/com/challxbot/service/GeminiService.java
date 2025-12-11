package com.challxbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class GeminiService {

    @Value("${google.ai.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        // Инициализация не требуется для HttpClient
    }

    public String generateLessonContent(String topicName, String lessonTitle) {
        log.info("🤖 AI REQUEST: Начинаю генерацию для темы '{}'", topicName);

        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ ОШИБКА AI: API Key не найден!");
            return "Ошибка конфигурации (API Key).";
        }

        String prompt = String.format(
                "Напиши короткий, веселый и понятный обучающий урок по теме '%s' для дисциплины '%s'. " +
                        "Используй форматирование Markdown (жирный шрифт, списки). " +
                        "Приведи 3 примера и 1 простое правило.",
                lessonTitle, topicName
        );

        // JSON Body
        String jsonBody = String.format(
            "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", 
            prompt.replace("\"", "\\\"").replace("\n", "\\n") // Экранирование кавычек
        );

        try {
            log.info("📡 Отправляю запрос в Google API (Java HttpClient)...");

            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
            
            // ИСПОЛЬЗУЕМ МОДЕЛЬ ИЗ СПИСКА: gemini-2.0-flash
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("❌ AI ERROR: Код ответа {}. Тело: {}", response.statusCode(), response.body());
                return "Ошибка API: " + response.statusCode();
            }

            String responseBody = response.body();
            log.info("✅ AI RESPONSE: Получен ответ! Длина: {}", responseBody.length());

            // Простой парсинг JSON с Jackson
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(responseBody);
            String text = root.at("/candidates/0/content/parts/0/text").asText();
            
            if (text == null || text.isEmpty()) {
                 return "Пустой ответ от AI.";
            }
            return text;

        } catch (Exception e) {
            log.error("❌ AI ERROR: Исключение при запросе.", e);
            return "Ошибка генерации: " + e.getMessage();
        }
    }
}