package com.challxbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${google.ai.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public String generateLessonContent(String topicName, String lessonTitle) {
        log.info("Asking Gemini to generate content for: {} - {}", topicName, lessonTitle);

        String prompt = String.format(
                "Напиши короткий, веселый и понятный обучающий урок по теме '%s' для дисциплины '%s'. " +
                        "Используй форматирование Markdown (жирный шрифт, списки). " +
                        "Приведи 3 примера и 1 простое правило.",
                lessonTitle, topicName
        );

        // Структура запроса к Gemini API
        var requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            // Отправляем запрос
            String response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Тут мы получаем сырой JSON, нам нужно бы его распарсить.
            // Но для простоты вернем пока "заглушку", если парсинг сложен без DTO.
            // В реальности тут нужно достать поле: candidates[0].content.parts[0].text
            // Давай пока вернем просто строку, чтобы проверить связь, или простейший парсинг.
            return extractTextFromJson(response);

        } catch (Exception e) {
            log.error("Gemini API Error", e);
            return "Не удалось сгенерировать контент. Попробуйте позже.";
        }
    }

    // Очень простой парсер, чтобы не тянуть DTO классы
    private String extractTextFromJson(String json) {
        // Это грязный хак для прототипа, в продакшене используем Jackson JsonNode!
        try {
            int startIndex = json.indexOf("\"text\": \"");
            if (startIndex == -1) return "Текст не найден в ответе ИИ";
            startIndex += 9;
            int endIndex = json.indexOf("\"", startIndex);
            // Нужно учитывать экранирование, но для теста сойдет
            String text = json.substring(startIndex, json.indexOf("\"", startIndex + 1)); // Упрощено
            // Внимание: лучше потом переписать на ObjectMapper, когда подключим библиотеку
            return "Gemini ответил (сырой текст пока сложно парсить вручную): " + text.substring(0, Math.min(text.length(), 50)) + "...";
        } catch (Exception e) {
            return "Ошибка парсинга ответа.";
        }
    }
}