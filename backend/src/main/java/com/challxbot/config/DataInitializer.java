package com.challxbot.config;

import com.challxbot.domain.Lesson;
import com.challxbot.domain.Topic;
import com.challxbot.repository.LessonRepository;
import com.challxbot.repository.TopicRepository;
import com.challxbot.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final GeminiService geminiService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Topic english = createTopicIfNotFound("English Language");
        createOrUpdateLesson(english, "The Verb 'to be'", 1);
        createTopicIfNotFound("Mathematics");
    }

    private Topic createTopicIfNotFound(String name) {
        return topicRepository.findByName(name).orElseGet(() -> {
            log.info("Creating topic: {}", name);
            Topic topic = Topic.builder().name(name).isActive(true).build();
            return topicRepository.save(topic);
        });
    }

    private void createOrUpdateLesson(Topic topic, String title, int order) {
        Optional<Lesson> existingLessonOpt = lessonRepository.findByTitle(title);

        boolean shouldGenerateContent = false;
        Lesson lesson;

        if (existingLessonOpt.isEmpty()) {
            log.info("Lesson '{}' not found. Creating and generating content.", title);
            shouldGenerateContent = true;
            lesson = Lesson.builder()
                    .topic(topic)
                    .title(title)
                    .orderIndex(order)
                    .build();
        } else {
            lesson = existingLessonOpt.get();
            String content = lesson.getContent();
            log.info("Current content for lesson '{}': {}", title, content); // ЛОГИРУЕМ КОНТЕНТ

            // Check if content is a placeholder or indicates an error
            if (content == null || content.contains("(AI generation failed, this is placeholder)") || content.startsWith("Ошибка") || content.contains("Не удалось сгенерировать контент")) {
                log.info("Lesson '{}' found with placeholder or error content. Regenerating.", title);
                shouldGenerateContent = true;
            } else {
                log.info("Lesson '{}' already exists with valid content. Skipping generation.", title);
            }
        }

        if (shouldGenerateContent) {
            String aiContent = geminiService.generateLessonContent(topic.getName(), title);

            // If AI fails, use a placeholder
            if (aiContent == null || aiContent.startsWith("Ошибка")) {
                log.warn("AI content generation failed for lesson '{}'. Using placeholder.", title);
                aiContent = "# The Verb 'to be'\\n\\nIs the most important verb in English! (AI generation failed, this is placeholder).";
            } else {
                log.info("Successfully generated AI content for lesson '{}'.", title);
            }
            lesson.setContent(aiContent);
            lessonRepository.save(lesson);
            log.info("Saved lesson: {}", title);
        }
    }
}