package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import utils.TestFilmUtils;
import utils.TestGenreUtils;
import utils.TestMpaUtils;
import utils.TestUserUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void filmValidationTest() {
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres())).isEmpty();
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().name(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().name("")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().description(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().description("")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().releaseDate(null)
                .build()))
                .hasSize(2);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().releaseDate(LocalDate.of(1894, 1, 1))
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().duration(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().duration(-10)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().genres(Set.of(Genre.builder().id(null).name("Genre null id").build()))
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().mpa(null)
                .build()))
                .isEmpty();
        assertThat(validator.validate(TestFilmUtils.getNewFilmWithMpaAndGenres()
                .toBuilder().mpa(Mpa.builder().id(null).build())
                .build()))
                .hasSize(1);
    }

    @Test
    void userValidationTest() {
        assertThat(validator.validate(TestUserUtils.getNewUser())).isEmpty();
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().email(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().email("")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().email("mail@@mail.ru")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().login(null)
                .build()))
                .hasSize(2);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().login("")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().login("lo gin")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().name("")
                .build()))
                .isEmpty();
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().name(null)
                .build()))
                .isEmpty();
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().birthday(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().birthday(LocalDate.of(1000, 1, 1))
                .build()))
                .isEmpty();
        assertThat(validator.validate(TestUserUtils.getNewUser()
                .toBuilder().birthday(LocalDate.of(9999, 1, 1))
                .build()))
                .hasSize(1);
    }

    @Test
    void genreValidationTest() {
        assertThat(validator.validate(TestGenreUtils.getNewGenre())).isEmpty();
        assertThat(validator.validate(TestGenreUtils.getNewGenre()
                .toBuilder().name(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestGenreUtils.getNewGenre()
                .toBuilder().name("")
                .build()))
                .hasSize(1);
    }

    @Test
    void mpaValidationTest() {
        assertThat(validator.validate(TestMpaUtils.getNewMpa())).isEmpty();
        assertThat(validator.validate(TestMpaUtils.getNewMpa()
                .toBuilder().name(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestMpaUtils.getNewMpa()
                .toBuilder().name("")
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestMpaUtils.getNewMpa()
                .toBuilder().description(null)
                .build()))
                .hasSize(1);
        assertThat(validator.validate(TestMpaUtils.getNewMpa()
                .toBuilder().description("")
                .build()))
                .isEmpty();
    }
}