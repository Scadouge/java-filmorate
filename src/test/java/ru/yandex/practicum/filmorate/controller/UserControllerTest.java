package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController controller;

    @Test
    void should_checkValidation() {
        assertDoesNotThrow(() -> controller.validateIdPost(null));
        assertDoesNotThrow(() -> controller.validateIdPost(1));
        assertThrows(ValidationException.class, () -> controller.validateIdPut(null));
        assertThrows(ValidationException.class, () -> controller.validateIdPut(1));
        assertThrows(ValidationException.class, () -> controller.validateLogin("lo gin"));
    }
}