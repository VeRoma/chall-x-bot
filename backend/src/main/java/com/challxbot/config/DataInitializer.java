package com.challxbot.config;

import com.challxbot.domain.Lesson;
import com.challxbot.domain.Topic;
import com.challxbot.repository.LessonRepository;
import com.challxbot.repository.TopicRepository;
import com.challxbot.service.GeminiService; // Добавили
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final GeminiService geminiService; // Подключаем ИИ

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Создаем тему "English Language"
        Topic english = createTopicIfNotFound("English Language");

        // 2. Создаем урок "Глагол to be"
        createLessonIfNotFound(english, "The Verb 'to be'", 1);

        // Математику тоже оставим
        createTopicIfNotFound("Mathematics");
    }

    private Topic createTopicIfNotFound(String name) {
        return topicRepository.findByName(name).orElseGet(() -> {
            Topic topic = Topic.builder().name(name).isActive(true).build();
            return topicRepository.save(topic);
        });
    }

    private void createLessonIfNotFound(Topic topic, String title, int order) {
        if (lessonRepository.findByTitle(title).isEmpty()) {
            log.info("Generating content for lesson: {}", title);

            // ВОТ ОНО! Спрашиваем у Gemini контент
            // Внимание: это может занять 2-3 секунды при запуске
            // Если ключа нет, упадет или вернет ошибку, не страшно.
            String aiContent = geminiService.generateLessonContent(topic.getName(), title);

            // Если ИИ сломался или ключа нет, ставим заглушку
            if (aiContent == null || aiContent.startsWith("Ошибка")) {
                aiContent = "# The Verb 'to be'\n\nIs the most important verb in English! (AI generation failed, this is placeholder).";
            }

            Lesson lesson = Lesson.builder()
                    .topic(topic)
                    .title(title)
                    .content(aiContent)
                    .orderIndex(order)
                    .build();

            lessonRepository.save(lesson);
            log.info("Initialized lesson: {}", title);
        }
    }
}