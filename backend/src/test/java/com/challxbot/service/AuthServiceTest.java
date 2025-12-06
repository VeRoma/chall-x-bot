package com.challxbot.service;

import com.challxbot.domain.User;
import com.challxbot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository; // Это наш "фейковый" репозиторий

    @InjectMocks
    private AuthService authService; // Это реальный сервис, куда "вколется" фейковый репозиторий

    @Test
    void shouldCreateNewUser_WhenUserDoesNotExist() {
        // 1. Подготовка (Given)
        Long tgId = 12345L;
        String username = "chall_fan";

        // Настраиваем мок: говорим, что при поиске по этому ID ничего не найдется
        when(userRepository.findByTgId(tgId)).thenReturn(Optional.empty());

        // Настраиваем мок: когда вызовут save, просто вернем тот же объект, что передали
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Действие (When) - вызываем реальный метод
        User result = authService.registerOrLogin(tgId, username, "TestName");

        // 3. Проверка (Then)
        assertNotNull(result);
        assertEquals(tgId, result.getTgId());
        assertEquals("USER", result.getRole()); // Проверяем, что роль по умолчанию проставилась

        // Проверяем, что метод сохранения в БД был вызван ровно 1 раз
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldReturnExistingUser_WhenUserExists() {
        // 1. Подготовка
        Long tgId = 999L;
        // Создаем готового юзера, как будто он из БД
        User existingUser = User.builder().tgId(tgId).username("old_user").build();

        // Настраиваем мок: говорим, что юзер уже есть
        when(userRepository.findByTgId(tgId)).thenReturn(Optional.of(existingUser));

        // 2. Действие
        User result = authService.registerOrLogin(tgId, "new_nick", "Name");

        // 3. Проверка
        assertEquals("old_user", result.getUsername()); // Должен вернуться СТАРЫЙ ник, а не новый

        // Самая важная проверка: метод save НЕ должен вызываться, так как мы ничего не меняли
        verify(userRepository, never()).save(any(User.class));
    }
}