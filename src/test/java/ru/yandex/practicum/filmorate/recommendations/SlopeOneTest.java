package ru.yandex.practicum.filmorate.recommendations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
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

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SlopeOneTest {
    private final MpaStorage mpaStorage;
    private final UserService userService;
    private final FilmService filmService;

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

        filmService.addMark(firstFilm.getId(), firstUser.getId(), 5);
        filmService.addMark(secondFilm.getId(), secondUser.getId(), 4);
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

        filmService.addMark(firstFilm.getId(), firstUser.getId(), 5);
        filmService.addMark(firstFilm.getId(), secondUser.getId(), 8);
        filmService.addMark(secondFilm.getId(), secondUser.getId(), 5);

        Collection<Film> rec = userService.getRecommendedFilms(firstUser.getId());
        assertThat(rec).isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("rating")
                .isEqualTo(new HashSet<>(List.of(secondFilm)));
    }
}
