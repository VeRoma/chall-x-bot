package com.challxbot.config;

import com.challxbot.domain.Topic;
import com.challxbot.domain.User;
import com.challxbot.repository.TopicRepository;
import com.challxbot.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Список стартовых тем
        List<String> defaultTopics = List.of("English Language", "Mathematics", "Java Programming");

        for (String topicName : defaultTopics) {
            createTopicIfNotFound(topicName);
        }

        if (userRepository.findById(1L).isEmpty()) {
            User testUser = User.builder()
                    .tgId(123456789L)
                    .username("test_teacher")
                    .firstName("Test User")
                    .starsBalance(1000L) // Дадим ему денег для тестов
                    .build();
            userRepository.save(testUser);
            log.info("Initialized test user: test_teacher (ID: 1)");
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