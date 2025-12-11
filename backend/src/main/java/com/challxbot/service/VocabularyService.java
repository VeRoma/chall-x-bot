package com.challxbot.service;

import com.challxbot.domain.Vocabulary;
import com.challxbot.repository.VocabularyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${google.ai.key}")
    private String apiKey;

    // Топ-30 слов для теста (можно расширить до 1000)
    private static final List<String> RAW_WORDS = List.of(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what"
    );

    // Модель AI
    private static final String MODEL_NAME = "gemini-1.5-flash";
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    /**
     * Запускает процесс обогащения слов.
     * Берет сырые слова -> Спрашивает AI -> Сохраняет в БД.
     */
    public void generateAndSaveVocabulary() {
        log.info("📚 Начало генерации словаря...");

        // Разбиваем на пачки по 10 слов, чтобы AI не сошел с ума
        int batchSize = 10;
        for (int i = 0; i < RAW_WORDS.size(); i += batchSize) {
            int end = Math.min(i + batchSize, RAW_WORDS.size());
            List<String> batch = RAW_WORDS.subList(i, end);

            processBatch(batch, i + 1); // i + 1 это текущий ранг (начало)

            try { Thread.sleep(2000); } catch (InterruptedException e) {} // Пауза
        }
        log.info("✅ Словарь загружен!");
    }

    private void processBatch(List<String> words, int startRank) {
        log.info("   ⏳ Обработка пачки: {}", words);

        // Промпт для AI
        String jsonStructure = "[{\"word\": \"...\", \"translationShort\": \"...\", \"translationFull\": \"...\", \"partOfSpeech\": \"...\", \"traps\": [\"trap1\", \"trap2\", \"trap3\"]}]";

        String prompt = String.format(
                "Я дам тебе список английских слов. Для каждого слова создай JSON объект. " +
                        "Список слов: %s. " +
                        "Требования: " +
                        "1. translationShort: перевод 1-2 слова (для кнопки). " +
                        "2. translationFull: полный перевод с примером использования. " +
                        "3. traps: массив из 3 НЕПРАВИЛЬНЫХ слов (на русском или английском), которые визуально похожи или созвучны, чтобы запутать. " +
                        "4. partOfSpeech: часть речи (verb, noun...). " +
                        "Верни ТОЛЬКО валидный JSON массив (без Markdown). Структура: %s",
                String.join(", ", words), jsonStructure
        );

        String jsonResponse = callGemini(prompt);
        if (jsonResponse == null) return;

        try {
            List<VocabularyDto> dtos = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            int currentRank = startRank;
            for (VocabularyDto dto : dtos) {
                // Проверяем, нет ли слова уже в базе
                if (vocabularyRepository.existsByWord(dto.word)) {
                    log.info("      Слово '{}' уже есть, пропускаем.", dto.word);
                    continue;
                }

                Vocabulary vocab = new Vocabulary(
                        dto.word,
                        dto.translationShort,
                        dto.translationFull,
                        currentRank++, // Присваиваем ранг по порядку
                        dto.partOfSpeech,
                        objectMapper.writeValueAsString(dto.traps) // Превращаем List в JSON-строку
                );
                vocabularyRepository.save(vocab);
            }
            log.info("      ✅ Пачка сохранена.");

        } catch (Exception e) {
            log.error("❌ Ошибка парсинга/сохранения пачки", e);
        }
    }

    private String callGemini(String prompt) {
        try {
            String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ");
            String jsonBody = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", escapedPrompt);
            String url = String.format(API_URL_TEMPLATE, MODEL_NAME, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("AI Error: {}", response.body());
                return null;
            }

            // Чистим JSON
            String text = objectMapper.readTree(response.body()).at("/candidates/0/content/parts/0/text").asText();
            text = text.trim();
            if (text.startsWith("```json")) text = text.substring(7);
            if (text.startsWith("```")) text = text.substring(3);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
            return text.trim();

        } catch (Exception e) {
            log.error("AI Request Failed", e);
            return null;
        }
    }

    // Внутренний класс для маппинга ответа AI
    private static class VocabularyDto {
        public String word;
        public String translationShort;
        public String translationFull;
        public String partOfSpeech;
        public List<String> traps;
    }
}