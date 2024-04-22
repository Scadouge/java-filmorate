package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.DbGenreStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.DbMpaStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestFilmUtils;
import utils.TestGenreUtils;
import utils.TestMpaUtils;
import utils.TestUserUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private GenreStorage genreStorage;
    private MpaStorage mpaStorage;

    @BeforeEach
    void setUp() {
        genreStorage = new DbGenreStorage(jdbcTemplate);
        mpaStorage = new DbMpaStorage(jdbcTemplate);
        userStorage = new DbUserStorage(jdbcTemplate);
        filmStorage = new DbFilmStorage(jdbcTemplate, genreStorage, mpaStorage);
    }

    private Film getNewFilmWithRandomMpaAndGenres() {
        return getNewFilmWithRandomMpaAndGenres(null);
    }

    private Film getNewFilmWithRandomMpaAndGenres(Long id) {
        Collection<Mpa> mpaCollection = mpaStorage.getAll();
        if (mpaCollection.size() == 0) {
            for (int i = 0; i < 10; i++) {
                mpaStorage.put(TestMpaUtils.getNewMpa());
            }
            mpaCollection = mpaStorage.getAll();
        }
        Mpa mpa = mpaCollection.stream().skip(new Random().nextInt(mpaCollection.size())).findFirst().orElse(null);

        Collection<Genre> genreCollection = genreStorage.getAll();
        if (genreCollection.size() == 0) {
            for (int i = 0; i < 10; i++) {
                genreStorage.put(TestGenreUtils.getNewGenre());
            }
            genreCollection = genreStorage.getAll();
        }

        Random random = new Random();
        return Film.builder()
                .id(id)
                .name(String.valueOf(random.nextInt(10000)))
                .description("Film desc")
                .releaseDate(LocalDate.of(2014, 10, random.nextInt(30) + 1))
                .duration(random.nextInt(30) + 1)
                .genres(genreCollection.stream().skip(new Random().nextInt(genreCollection.size())).collect(Collectors.toSet()))
                .mpa(mpa).build();
    }

    @Test
    void testPutFilm() {
        final Film newFilmWrongGenre = getNewFilmWithRandomMpaAndGenres().toBuilder()
                .genres(Set.of(TestGenreUtils.getNewNonExistentGenre())).build();
        assertThrows(ValidationException.class, () -> filmStorage.put(newFilmWrongGenre));

        final Film newFilmWrongMpa = getNewFilmWithRandomMpaAndGenres().toBuilder()
                .mpa(TestMpaUtils.getNewNonExistentMpa()).build();
        assertThrows(ValidationException.class, () -> filmStorage.put(newFilmWrongMpa));
    }

    @Test
    void testGetFilmById() {
        final Film newFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());
        final Film savedFilm = filmStorage.get(newFilm.getId());
        final Film newFilmWithoutMpa = filmStorage.put(getNewFilmWithRandomMpaAndGenres()
                .toBuilder().genres(Set.of()).mpa(null).build());

        assertTrue(newFilmWithoutMpa.getGenres().isEmpty());
        assertNull(newFilmWithoutMpa.getMpa());

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newFilm);
        assertThrows(ItemNotFoundException.class, () -> filmStorage.get(TestFilmUtils.getNonExistedFilm().getId()));
    }

    @Test
    void testGetAllFilms() {
        final Film firstFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());
        final Film secondFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());

        Collection<Film> allFilms = filmStorage.getAll();
        assertTrue(allFilms.contains(firstFilm));
        assertTrue(allFilms.contains(secondFilm));
    }

    @Test
    void testUpdateFilm() {
        final Film newFilm = getNewFilmWithRandomMpaAndGenres();
        final Long newFilmId = filmStorage.put(newFilm).getId();
        final Film toUpdateFilm = getNewFilmWithRandomMpaAndGenres(newFilmId);
        filmStorage.update(toUpdateFilm);

        final Film savedFilm = filmStorage.get(newFilmId);

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateFilm);
        assertThrows(ItemNotFoundException.class, () -> filmStorage.update(TestFilmUtils.getNonExistedFilm()));
    }

    @Test
    void testDeleteFilm() {
        final Film newFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());
        filmStorage.get(newFilm.getId());
        filmStorage.delete(newFilm);
        assertThrows(ItemNotFoundException.class, () -> filmStorage.get(newFilm.getId()));
        assertThrows(ItemNotFoundException.class, () -> filmStorage.get(TestFilmUtils.getNonExistedFilm().getId()));
    }

    @Test
    void testAddLike() {
        final Film newFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());

        assertThrows(DataIntegrityViolationException.class,
                () -> filmStorage.addLike(newFilm, TestUserUtils.getNewNonExistentUser()));

        final User newUser = userStorage.put(TestUserUtils.getNewUser());

        assertThrows(DataIntegrityViolationException.class,
                () -> filmStorage.addLike(TestFilmUtils.getNonExistedFilm(), newUser));
        assertDoesNotThrow(() -> filmStorage.addLike(newFilm, newUser));

        final int likes = filmStorage.getLikesCount(newFilm);

        assertEquals(0, filmStorage.getLikesCount(TestFilmUtils.getNonExistedFilm()));
        assertEquals(1, likes);
    }

    @Test
    void testRemoveLike() {
        final Film newFilm = filmStorage.put(getNewFilmWithRandomMpaAndGenres());
        final User newUser = userStorage.put(TestUserUtils.getNewUser());

        filmStorage.addLike(newFilm, newUser);

        assertEquals(1, filmStorage.getLikesCount(newFilm));
        assertDoesNotThrow(() -> filmStorage.removeLike(newFilm, TestUserUtils.getNewNonExistentUser()));
        assertEquals(1, filmStorage.getLikesCount(newFilm));

        filmStorage.removeLike(newFilm, newUser);

        assertEquals(0, filmStorage.getLikesCount(newFilm));
        assertDoesNotThrow(() -> filmStorage.removeLike(TestFilmUtils.getNonExistedFilm(), newUser));
    }

    @Test
    void testGetPopularByLikes() {
        Film firstFilm = getNewFilmWithRandomMpaAndGenres();
        firstFilm = filmStorage.put(firstFilm);
        Film secondFilm = getNewFilmWithRandomMpaAndGenres().toBuilder().mpa(null).build();
        secondFilm = filmStorage.put(secondFilm);
        final User firstUser = userStorage.put(TestUserUtils.getNewUser());
        final User secondUser = userStorage.put(TestUserUtils.getNewUser());

        filmStorage.addLike(secondFilm, firstUser);
        filmStorage.addLike(secondFilm, secondUser);
        filmStorage.addLike(firstFilm, secondUser);

        Collection<Film> popularByLikesMaxOne = filmStorage.getPopularByLikes(1);

        assertEquals(1, popularByLikesMaxOne.size());
        assertTrue(popularByLikesMaxOne.contains(secondFilm));

        Collection<Film> popularByLikes = filmStorage.getPopularByLikes(10);
        Optional<Film> mostPopularFilm = popularByLikes.stream().findFirst();
        Optional<Film> almostPopularFilm = popularByLikes.stream().skip(1).findFirst();

        assertTrue(mostPopularFilm.isPresent());
        assertTrue(almostPopularFilm.isPresent());
        assertEquals(secondFilm, mostPopularFilm.get());
        assertEquals(firstFilm, almostPopularFilm.get());
        assertEquals(2, popularByLikes.size());
    }
}