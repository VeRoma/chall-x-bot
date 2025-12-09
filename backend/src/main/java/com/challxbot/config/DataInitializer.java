package com.challxbot.config;

import com.challxbot.domain.Topic;
import com.challxbot.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TopicRepository topicRepository;

    @Override
    public void run(String... args) throws Exception {
        // Список стартовых тем
        List<String> defaultTopics = List.of("English Language", "Mathematics", "Java Programming");

        for (String topicName : defaultTopics) {
            createTopicIfNotFound(topicName);
        }
    }

    private void createTopicIfNotFound(String name) {
        if (topicRepository.findByName(name).isEmpty()) {
            Topic topic = Topic.builder()
                    .name(name)
                    .isActive(true)
                    .build();
            topicRepository.save(topic);
            log.info("Initialized new topic: {}", name);
        }
    }
}