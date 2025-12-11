package com.challxbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class GeminiService {

    @Value("${google.ai.key}")
    private String apiKey;

    // 🔥 ФИКСИРУЕМ МОДЕЛЬ, КАК ВЫ ПРОСИЛИ
    private static final String MODEL_NAME = "gemini-3-pro-preview";
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
    }

    /**
     * Генерирует УРОК (HTML).
     * Теперь строго на РУССКОМ языке для объяснений.
     */
    public String generateLessonContent(String topicName, String lessonTitle) {
        log.info("🤖 AI REQUEST: HTML Урок (RU) по теме '{}'", topicName);

        String prompt = String.format(
                "Ты — учитель английского для русскоязычных новичков (уровень A1). " +
                        "Напиши веселый и простой урок по теме '%s' (%s). " +
                        "ВАЖНО: \n" +
                        "1. Весь объясняющий текст пиши НА РУССКОМ ЯЗЫКЕ.\n" +
                        "2. Английские примеры давай с переводом.\n" +
                        "3. Верни ТОЛЬКО HTML код (внутри <div>).\n" +
                        "4. Используй теги: <div class='lesson-card'>, <h2>, <p>, <ul>, <li>, <span class='highlight'>.\n" +
                        "5. Используй эмодзи.",
                lessonTitle, topicName
        );

        return sendRequestToGemini(prompt, false);
    }

    /**
     * Генерирует КВИЗ (JSON).
     * Тоже просим вопросы на понятном языке (или на английском, но с русскими пояснениями).
     */
    public String generateQuiz(String topicName) {
        log.info("🤖 AI REQUEST: JSON Quiz (RU) для темы '{}'", topicName);

        String prompt = String.format(
                "Создай тест из 5 вопросов по теме '%s' для начинающих. " +
                        "Вопросы могут быть на английском (простые), но объяснения (explanation) пиши СТРОГО НА РУССКОМ. " +
                        "Верни ТОЛЬКО валидный JSON массив. " +
                        "Структура: " +
                        "{ \"question\": \"Question text?\", " +
                        "\"options\": [\"Option A\", \"Option B\", \"Option C\"], " +
                        "\"correctIndex\": 0, " +
                        "\"explanation\": \"Почему это правильно (на русском).\" }",
                topicName
        );

        return sendRequestToGemini(prompt, true);
    }

    private String sendRequestToGemini(String prompt, boolean isJsonExpected) {
        if (apiKey == null || apiKey.isBlank()) {
            return "<p style='color:red'>Ошибка: API Key не найден.</p>";
        }

        try {
            // Экранирование
            String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            String jsonBody = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", escapedPrompt);

            String url = String.format(API_URL_TEMPLATE, MODEL_NAME, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            log.info("📡 Отправляю запрос в Google API (Model: {})...", MODEL_NAME);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("❌ AI Error: {}", response.body());
                return "<p>Ошибка AI: " + response.statusCode() + "</p>";
            }

            String rawText = extractTextFromJson(response.body());

            if (isJsonExpected) {
                return cleanJson(rawText);
            } else {
                return cleanHtmlMarkdown(rawText);
            }

        } catch (Exception e) {
            log.error("❌ Exception", e);
            return isJsonExpected ? "[]" : "<p>Ошибка генерации контента.</p>";
        }
    }

    private String cleanJson(String text) {
        text = text.trim();
        if (text.startsWith("```json")) text = text.substring(7);
        else if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
        return text.trim();
    }

    private String cleanHtmlMarkdown(String text) {
        text = text.trim();
        if (text.startsWith("```html")) text = text.substring(7);
        else if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
        return text.trim();
    }

    private String extractTextFromJson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            return textNode.isMissingNode() ? "" : textNode.asText();
        } catch (Exception e) {
            return "";
        }
    }
}