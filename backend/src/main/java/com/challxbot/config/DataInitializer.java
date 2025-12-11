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

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final GeminiService geminiService;

    // Список тем (оставляем для справки)
    private final List<String> TOPICS = List.of(
            "The Verb 'to be' (Глагол быть)",
            "Present Simple (Настоящее простое)",
            "Present Continuous (Настоящее продолженное)",
            "Past Simple (Прошедшее простое)",
            "Future Simple (Will vs Going to)",
            "Articles (A/An/The)",
            "Plural Nouns (Множественное число)",
            "Pronouns (Местоимения)",
            "Adjectives (Прилагательные)",
            "Prepositions of Place (In/On/At)",
            "Modal Verbs (Can/Must/Should)"
    );

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // ПРОВЕРКА: Если темы уже есть, ничего не делаем
            long topicCount = topicRepository.count();
            if (topicCount > 0) {
                log.info("🚀 База данных уже заполнена (найдено {} тем). Пропускаем генерацию.", topicCount);
                return;
            }

            // ... (Код генерации, который был раньше, сработает только если база пустая) ...
            // Можно оставить старый код ниже внутри блока if (topicCount == 0) { ... }
            // Но пока просто закомментируем или оставим return выше.

            log.info("⚠️ База пуста! Раскомментируйте код генерации, если хотите создать уроки заново.");
        };
    }
}