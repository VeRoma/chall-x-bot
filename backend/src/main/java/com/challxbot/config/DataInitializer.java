package com.challxbot.config;

import com.challxbot.domain.Lesson;
import com.challxbot.domain.Topic;
import com.challxbot.repository.LessonRepository;
import com.challxbot.repository.TopicRepository;
import com.challxbot.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final GeminiService geminiService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // ВАЖНО: Удаляем старые уроки, чтобы сгенерировать новые (красивые HTML)
            // В продакшене эту строку нужно будет убрать!
            lessonRepository.deleteAll();
            topicRepository.deleteAll();

            // Создаем тему
            String topicName = "English Language";
            Topic topic = topicRepository.findByName(topicName)
                    .orElseGet(() -> {
                        Topic newTopic = Topic.builder()
                                .name(topicName)
                                .isActive(true)
                                .build();
                        return topicRepository.save(newTopic);
                    });

            // Генерируем урок
            String lessonTitle = "The Verb 'to be'";

            try {
                String content = geminiService.generateLessonContent(topicName, lessonTitle);

                Lesson lesson = Lesson.builder()
                        .title(lessonTitle)
                        .content(content)
                        .topic(topic)
                        .orderIndex(1)
                        .build();

                lessonRepository.save(lesson);
                log.info("✅ Урок '{}' успешно создан!", lessonTitle);

            } catch (Exception e) {
                log.error("❌ Не удалось сгенерировать или сохранить урок '{}'", lessonTitle, e);
            }
        };
    }
}