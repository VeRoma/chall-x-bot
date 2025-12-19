package com.challxbot.service;

import com.challxbot.domain.Vocabulary;
import com.challxbot.repository.VocabularyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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

    // Ссылка на частотный словарь
    private static final String WIKTIONARY_URL = "https://en.wiktionary.org/wiki/Wiktionary:Frequency_lists/PG/2005/08/1-10000";

    // Используем проверенную модель
    private static final String MODEL_NAME = "gemini-3-pro-preview";
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    /**
     * Запускает процесс: Парсинг -> Фильтрация -> Обогащение AI -> Сохранение
     */
    public void generateAndSaveVocabulary() {
        log.info("🌍 Подключаюсь к Wiktionary для парсинга слов...");

        try {
            // 1. Парсим слова с сайта (лимит 100, чтобы быстро проверить)
            List<String> rawWords = fetchWordsFromWiktionary(100);

            if (rawWords.isEmpty()) {
                log.error("❌ Слов не найдено! Проверьте структуру сайта.");
                return;
            }

            log.info("✅ Найдено {} слов (по ссылкам). Начинаем проверку и загрузку...", rawWords.size());

            // 2. Разбиваем на пачки по 10 слов
            int batchSize = 10;
            for (int i = 0; i < rawWords.size(); i += batchSize) {
                int end = Math.min(i + batchSize, rawWords.size());
                List<String> batch = rawWords.subList(i, end);

                // i + 1 это текущий ранг
                processBatch(batch, i + 1);
            }
            log.info("🎉 Загрузка словаря завершена!");

        } catch (Exception e) {
            log.error("❌ Критическая ошибка при загрузке словаря", e);
            // Никаких fallback'ов — если упало, значит упало.
        }
    }

    /**
     * Парсит страницу, извлекая слова из тегов <a> внутри списков.
     */
    private List<String> fetchWordsFromWiktionary(int limit) throws Exception {
        List<String> words = new ArrayList<>();

        // Скачиваем HTML
        Document doc = Jsoup.connect(WIKTIONARY_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        // Логика: На странице слова идут в параграфах <p> после заголовков <h3>.
        // Слова обернуты в <a href="/wiki/word" title="word">word</a>

        // Берем все параграфыЫ
        Elements paragraphs = doc.select(".mw-parser-output > p");

        for (Element p : paragraphs) {
            // Берем все ссылки внутри параграфа
            Elements links = p.select("a");

            // Если в параграфе мало ссылок, скорее всего это обычный текст, а не список слов.
            // В списках слов обычно > 5 ссылок подряд.
            if (links.size() < 5) continue;

            for (Element link : links) {
                String word = link.text().trim();
                String href = link.attr("href");

                // Фильтрация мусора:
                // 1. Исключаем ссылки на редактирование, служебные страницы и пустые строки
                // 2. Исключаем слова с цифрами
                if (!word.isEmpty()
                        && !word.equalsIgnoreCase("edit")
                        && !href.contains("action=edit")
                        && !href.contains("Special:")
                        && !word.matches(".*\\d.*")) {

                    words.add(word);

                    // TODO: В будущем здесь можно сохранить href (ссылку),
                    // чтобы потом зайти на страницу слова и скачать аудио.

                    if (words.size() >= limit) return words;
                }
            }
        }

        // Если вообще ничего не нашли — кидаем ошибку
        if (words.isEmpty()) {
            throw new RuntimeException("Парсер не нашел ни одной ссылки с классом <a> в предполагаемых списках.");
        }

        return words;
    }

    private void processBatch(List<String> originalBatch, int startRank) {
        // 1. Фильтруем: оставляем только те, которых нет в БД
        List<String> missingWords = new ArrayList<>();
        for (String word : originalBatch) {
            if (!vocabularyRepository.existsByWord(word)) {
                missingWords.add(word);
            }
        }

        if (missingWords.isEmpty()) {
            log.info("   ⏩ Пачка {}-{} уже в базе. Пропускаем.", startRank, startRank + originalBatch.size() - 1);
            return;
        }

        log.info("   ⏳ Загружаю новые слова ({} шт)...", missingWords.size());

        String jsonStructure = "[{\"word\": \"...\", \"translationShort\": \"...\", \"translationFull\": \"...\", \"partOfSpeech\": \"...\", \"traps\": [\"trap1\", \"trap2\", \"trap3\"]}]";

        String prompt = String.format(
                "Я дам тебе список английских слов. Для каждого слова создай JSON объект. " +
                        "Список слов: %s. " +
                        "Требования: " +
                        "1. translationShort: перевод 1-2 слова (для кнопки). " +
                        "2. translationFull: полный перевод с примером использования. " +
                        "3. traps: массив из 3 НЕПРАВИЛЬНЫХ слов (на русском!), которые на английском визуально похожи или созвучны с исходным словом, чтобы запутать. " +
                        "4. partOfSpeech: часть речи (verb, noun...). " +
                        "Верни ТОЛЬКО валидный JSON массив (без Markdown). Структура: %s",
                String.join(", ", missingWords), jsonStructure
        );

        String jsonResponse = callGemini(prompt);
        if (jsonResponse == null || jsonResponse.isEmpty()) return;

        try {
            List<VocabularyDto> dtos = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            for (VocabularyDto dto : dtos) {
                int indexInBatch = originalBatch.indexOf(dto.word);
                if (indexInBatch == -1) continue;

                int correctRank = startRank + indexInBatch;

                Vocabulary vocab = new Vocabulary(
                        dto.word,
                        dto.translationShort,
                        dto.translationFull,
                        correctRank,
                        dto.partOfSpeech,
                        objectMapper.writeValueAsString(dto.traps),
                        null
                );
                vocabularyRepository.save(vocab);
            }
            log.info("      ✅ Сохранено {} новых слов.", dtos.size());
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("❌ Ошибка обработки ответа AI", e);
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
                log.error("AI Error {}: {}", response.statusCode(), response.body());
                return null;
            }
            return extractTextFromJson(response.body());
        } catch (Exception e) {
            log.error("Request Failed", e);
            return null;
        }
    }

    private String extractTextFromJson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode()) return "";
            String text = textNode.asText().trim();
            if (text.startsWith("```json")) text = text.substring(7);
            else if (text.startsWith("```")) text = text.substring(3);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
            return text.trim();
        } catch (Exception e) { return ""; }
    }

    private static class VocabularyDto {
        public String word;
        public String translationShort;
        public String translationFull;
        public String partOfSpeech;
        public List<String> traps;
    }
}