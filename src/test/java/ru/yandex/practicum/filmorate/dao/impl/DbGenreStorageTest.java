package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.DbGenreStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.DbMpaStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import utils.TestGenreUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbGenreStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private GenreStorage genreStorage;
    private FilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        genreStorage = new DbGenreStorage(jdbcTemplate);
        MpaStorage mpaStorage = new DbMpaStorage(jdbcTemplate);
        filmStorage = new DbFilmStorage(jdbcTemplate, genreStorage, mpaStorage);
    }

    @Test
    void testPutGenre() {
        Genre newGenre = TestGenreUtils.getNewGenre();
        newGenre = genreStorage.put(newGenre);
        final Genre savedGenre = genreStorage.get(newGenre.getId());

        assertThat(savedGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newGenre);
    }

    @Test
    void testGetGenre() {
        final Genre nonExistentGenre = TestGenreUtils.getNewNonExistentGenre();
        assertThrows(ItemNotFoundException.class, () -> genreStorage.get(nonExistentGenre.getId()));
        final Genre newGenre = genreStorage.put(TestGenreUtils.getNewGenre());
        assertDoesNotThrow(() -> genreStorage.get(newGenre.getId()));
    }

    @Test
    void testGetAllGenre() {
        final Genre newGenre = genreStorage.put(TestGenreUtils.getNewGenre());
        Collection<Genre> allGenres = genreStorage.getAll();

        assertTrue(allGenres.contains(newGenre));
    }

    @Test
    void testUpdateMpa() {
        final Genre newGenre = genreStorage.put(TestGenreUtils.getNewGenre());
        final Genre toUpdateGenre = TestGenreUtils.getNewGenre(newGenre.getId());
        genreStorage.update(toUpdateGenre);
        final Genre savedGenre = genreStorage.get(newGenre.getId());

        assertThat(savedGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateGenre);
    }

    @Test
    void testDeleteMpa() {
        final Genre newGenre = genreStorage.put(TestGenreUtils.getNewGenre());
        final Film newFilm = Film.builder()
                .name("Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2012, 12, 1))
                .duration(100)
                .genres(Set.of(newGenre))
                .build();
        filmStorage.put(newFilm);

        assertDoesNotThrow(() -> genreStorage.delete(newGenre));
        assertThrows(ItemNotFoundException.class, () -> genreStorage.get(newGenre.getId()));
    }
}