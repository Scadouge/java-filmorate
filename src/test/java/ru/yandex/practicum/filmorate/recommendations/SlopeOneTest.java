package ru.yandex.practicum.filmorate.recommendations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.director.DbDirectorStorage;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.DbGenreStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.DbMpaStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import utils.TestFilmUtils;
import utils.TestMpaUtils;
import utils.TestUserUtils;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SlopeOneTest {
    private final JdbcTemplate jdbcTemplate;
    private MpaStorage mpaStorage;
    private UserService userService;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        mpaStorage = new DbMpaStorage(jdbcTemplate);
        DirectorStorage directorStorage = new DbDirectorStorage(jdbcTemplate);
        GenreStorage genreStorage = new DbGenreStorage(jdbcTemplate);
        FilmStorage filmStorage = new DbFilmStorage(jdbcTemplate, genreStorage, mpaStorage, directorStorage);
        UserStorage userStorage = new DbUserStorage(jdbcTemplate);

        filmService = new FilmService(filmStorage, userStorage, directorStorage);
        userService = new UserService(userStorage, filmService);
    }

    private Film getNewFilm() {
        Collection<Mpa> mpaCollection = mpaStorage.getAll();
        if (mpaCollection.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                mpaStorage.put(TestMpaUtils.getNewMpa());
            }
            mpaCollection = mpaStorage.getAll();
        }
        Mpa mpa = mpaCollection.stream()
                .skip(new Random().nextInt(mpaCollection.size()))
                .findFirst().orElse(null);
        return TestFilmUtils.getNewFilm()
                .toBuilder().mpa(mpa).build();
    }

    @Test
    @DisplayName("возвращает пустой список если для пользователя нет совпадений по лайкам")
    void shouldReturnEmptyListWhenFilmNotFound() {
        Film firstFilm = filmService.addFilm(getNewFilm());
        Film secondFilm = filmService.addFilm(getNewFilm());
        User firstUser = userService.addUser(TestUserUtils.getNewUser());
        User secondUser = userService.addUser(TestUserUtils.getNewUser());

        Collection<Film> rec = userService.getRecommendedFilms(firstUser.getId());
        assertThat(rec).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_SET);

        filmService.addLike(firstFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());
        rec = userService.getRecommendedFilms(firstUser.getId());
        assertThat(rec).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_SET);
    }

    @Test
    @DisplayName("возвращает список рекомендаций для пользователей с общимим лайками")
    void shouldReturnListRecommendations() {
        Film firstFilm = filmService.addFilm(getNewFilm());
        Film secondFilm = filmService.addFilm(getNewFilm());
        User firstUser = userService.addUser(TestUserUtils.getNewUser());
        User secondUser = userService.addUser(TestUserUtils.getNewUser());

        filmService.addLike(firstFilm.getId(), firstUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());

        Collection<Film> rec = userService.getRecommendedFilms(firstUser.getId());
        assertThat(rec).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(new HashSet<>(List.of(secondFilm)));
    }
}
