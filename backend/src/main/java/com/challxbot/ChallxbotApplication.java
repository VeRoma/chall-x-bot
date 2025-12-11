package com.challxbot;

import lombok.extern.slf4j.Slf4j; // Не забудьте добавить Lombok аннотацию
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j // Включаем логирование
@SpringBootApplication
public class ChallxbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallxbotApplication.class, args);
    }

    // Этот метод выполнится сразу после запуска сервера
    @Bean
    public CommandLineRunner connectionCheck(DataSource dataSource) {
        return args -> {
            log.info("----------------------------------------");
            log.info("🚀 ПРОВЕРКА ПОДКЛЮЧЕНИЙ ПРИ СТАРТЕ:");

            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(1)) {
                    log.info("✅ База данных (PostgreSQL): ПОДКЛЮЧЕНО УСПЕШНО!");
                    log.info("URL: " + conn.getMetaData().getURL());
                } else {
                    log.error("❌ База данных: Соединение невалидно.");
                }
            } catch (Exception e) {
                log.error("❌ База данных: ОШИБКА ПОДКЛЮЧЕНИЯ", e);
            }
            log.info("----------------------------------------");
        };
    }
}