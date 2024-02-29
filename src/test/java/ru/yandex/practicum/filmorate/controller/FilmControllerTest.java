package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmControllerTest {

    @Autowired
    private FilmController controller;

    @Test
    void should_checkValidation() {
//        assertDoesNotThrow(() -> controller.validateIdPost(null));
//        assertDoesNotThrow(() -> controller.validateIdPost(1));
//        assertThrows(ValidationException.class, () -> controller.validateIdPut(null));
//        assertThrows(ValidationException.class, () -> controller.validateIdPut(1));
//        assertThrows(ValidationException.class, () -> controller.validateReleaseDate(LocalDate.of(1000,1,1)));
//        assertDoesNotThrow(() -> controller.validateReleaseDate(LocalDate.of(2000,1,1)));
    }
}