package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.director.DbDirectorStorage;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.event.DbEventStorage;
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
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import utils.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private MpaStorage mpaStorage;
    private GenreStorage genreStorage;
    private DirectorStorage directorStorage;
    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        genreStorage = new DbGenreStorage(jdbcTemplate);
        mpaStorage = new DbMpaStorage(jdbcTemplate);
        directorStorage = new DbDirectorStorage(jdbcTemplate);
        filmStorage = new DbFilmStorage(jdbcTemplate, genreStorage, mpaStorage, directorStorage);
        UserStorage userStorage = new DbUserStorage(jdbcTemplate);
        EventService eventService = new EventService(new DbEventStorage(jdbcTemplate), userStorage);
        filmService = new FilmService(filmStorage, userStorage, directorStorage, eventService);
        userService = new UserService(userStorage, filmService, eventService);
    }

    private Film getNewFilledFilm() {
        return getNewFilledFilm(null);
    }

    private Film getNewFilledFilm(Long id) {
        Collection<Mpa> mpaCollection = mpaStorage.getAll();
        if (mpaCollection.size() < 5) {
            for (int i = 0; i < 5; i++) {
                mpaStorage.put(TestMpaUtils.getNewMpa());
            }
            mpaCollection = mpaStorage.getAll();
        }
        Mpa mpa = mpaCollection.stream().skip(new Random().nextInt(mpaCollection.size())).findFirst().orElse(null);

        Collection<Genre> genreCollection = genreStorage.getAll();
        if (genreCollection.size() < 5) {
            for (int i = 0; i < 5; i++) {
                genreStorage.put(TestGenreUtils.getNewGenre());
            }
            genreCollection = genreStorage.getAll();
        }

        Collection<Director> directorCollection = directorStorage.getAll();
        if (directorCollection.size() < 5) {
            for (int i = 0; i < 5; i++) {
                directorStorage.put(TestDirectorUtils.getNewDirector());
            }
            directorCollection = directorStorage.getAll();
        }

        Random random = new Random();
        return Film.builder()
                .id(id)
                .name(String.valueOf(random.nextInt(10000)))
                .description("Film desc")
                .releaseDate(LocalDate.of(2014, 10, random.nextInt(30) + 1))
                .duration(random.nextInt(30) + 1)
                .genres(genreCollection.stream().skip(new Random().nextInt(genreCollection.size())).collect(Collectors.toSet()))
                .directors(directorCollection.stream().skip(new Random().nextInt(directorCollection.size())).collect(Collectors.toSet()))
                .mpa(mpa)
                .build();
    }

    @Test
    void testPutFilm() {
        final Film newFilmWrongGenre = getNewFilledFilm().toBuilder()
                .genres(Set.of(TestGenreUtils.getNewNonExistentGenre())).build();
        assertThrows(ValidationException.class, () -> filmService.addFilm(newFilmWrongGenre));

        final Film newFilmWrongMpa = getNewFilledFilm().toBuilder()
                .mpa(TestMpaUtils.getNewNonExistentMpa()).build();
        assertThrows(ValidationException.class, () -> filmService.addFilm(newFilmWrongMpa));
    }

    @Test
    void testGetFilmById() {
        final Film newFilm = filmService.addFilm(getNewFilledFilm());
        final Film savedFilm = filmService.getFilm(newFilm.getId());
        final Film newFilmWithoutMpa = filmService.addFilm(getNewFilledFilm()
                .toBuilder().genres(Set.of()).mpa(null).build());

        assertTrue(newFilmWithoutMpa.getGenres().isEmpty());
        assertNull(newFilmWithoutMpa.getMpa());

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newFilm);
        assertThrows(ItemNotFoundException.class, () -> filmService.getFilm(TestFilmUtils.getNonExistedFilm().getId()));
    }

    @Test
    void testGetAllFilms() {
        final Film firstFilm = filmService.addFilm(getNewFilledFilm());
        final Film secondFilm = filmService.addFilm(getNewFilledFilm());

        Collection<Film> allFilms = filmService.getAllFilms();
        assertTrue(allFilms.contains(firstFilm));
        assertTrue(allFilms.contains(secondFilm));
    }

    @Test
    void testUpdateFilm() {
        final Film newFilm = getNewFilledFilm();
        final Long newFilmId = filmService.addFilm(newFilm).getId();
        final Film toUpdateFilm = getNewFilledFilm(newFilmId);
        filmService.updateFilm(toUpdateFilm);

        final Film savedFilm = filmService.getFilm(newFilmId);

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateFilm);
        assertThrows(ItemNotFoundException.class, () -> filmService.updateFilm(TestFilmUtils.getNonExistedFilm()));
    }

    @Test
    void testDeleteFilm() {
        final Film newFilm = filmService.addFilm(getNewFilledFilm());
        filmService.getFilm(newFilm.getId());
        filmService.deleteFilm(newFilm.getId());
        assertThrows(ItemNotFoundException.class, () -> filmService.getFilm(newFilm.getId()));
        assertThrows(ItemNotFoundException.class, () -> filmService.getFilm(TestFilmUtils.getNonExistedFilm().getId()));
    }

    @Test
    void testAddLike() {
        final Film newFilm = filmService.addFilm(getNewFilledFilm());

        assertThrows(ItemNotFoundException.class,
                () -> filmService.addLike(newFilm.getId(), TestUserUtils.getNewNonExistentUser().getId()));

        final User newUser = userService.addUser(TestUserUtils.getNewUser());

        assertThrows(ItemNotFoundException.class,
                () -> filmService.addLike(TestFilmUtils.getNonExistedFilm().getId(), newUser.getId()));
        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), newUser.getId()));
        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), newUser.getId()));
        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), newUser.getId()));

        final User newUser2 = userService.addUser(TestUserUtils.getNewUser());

        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), newUser2.getId()));

        assertEquals(2, filmStorage.getLikesCount(newFilm));
        assertEquals(0, filmStorage.getLikesCount(TestFilmUtils.getNonExistedFilm()));

        userService.deleteUser(newUser.getId());
        assertThrows(ItemNotFoundException.class, () -> userService.deleteUser(newUser.getId()));
        assertThrows(ItemNotFoundException.class, () -> userService.deleteUser(newUser.getId()));

        assertEquals(1, filmStorage.getLikesCount(newFilm));
    }

    @Test
    void testRemoveLike() {
        final Film newFilm = filmService.addFilm(getNewFilledFilm());
        final User newUser = userService.addUser(TestUserUtils.getNewUser());

        filmService.addLike(newFilm.getId(), newUser.getId());

        assertEquals(1, filmStorage.getLikesCount(newFilm));
        assertThrows(ItemNotFoundException.class, () -> filmService.removeLike(newFilm.getId(), TestUserUtils.getNewNonExistentUser().getId()));
        assertEquals(1, filmStorage.getLikesCount(newFilm));

        filmService.removeLike(newFilm.getId(), newUser.getId());

        assertEquals(0, filmStorage.getLikesCount(newFilm));
        assertThrows(ItemNotFoundException.class, () -> filmService.removeLike(TestFilmUtils.getNonExistedFilm().getId(), newUser.getId()));
    }

    @Test
    void testGetPopularByLikes() {
        Film firstFilm = getNewFilledFilm();
        firstFilm = filmService.addFilm(firstFilm);
        Film secondFilm = getNewFilledFilm().toBuilder().mpa(null).build();
        secondFilm = filmService.addFilm(secondFilm);
        final User firstUser = userService.addUser(TestUserUtils.getNewUser());
        final User secondUser = userService.addUser(TestUserUtils.getNewUser());

        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());

        Collection<Film> popularByLikesMaxOne = filmService.getPopularByYearAndGenre(1, null, null);

        assertEquals(1, popularByLikesMaxOne.size());
        assertTrue(popularByLikesMaxOne.contains(secondFilm));

        Collection<Film> popularByLikes = filmService.getPopularByYearAndGenre(10, null, null);
        Optional<Film> mostPopularFilm = popularByLikes.stream().findFirst();
        Optional<Film> almostPopularFilm = popularByLikes.stream().skip(1).findFirst();

        assertTrue(mostPopularFilm.isPresent());
        assertTrue(almostPopularFilm.isPresent());
        assertEquals(secondFilm, mostPopularFilm.get());
        assertEquals(firstFilm, almostPopularFilm.get());
        assertEquals(2, popularByLikes.size());
    }

    @Test
    void testGetSortedDirectorFilms() {
        final Director director = directorStorage.put(TestDirectorUtils.getNewDirector());
        Film firstFilm = getNewFilledFilm().toBuilder().genres(new HashSet<>()).directors(Set.of(director))
                .releaseDate(LocalDate.of(1000, 1, 1)).build();
        firstFilm = filmService.addFilm(firstFilm);
        Film secondFilm = getNewFilledFilm().toBuilder().mpa(null).directors(Set.of(director))
                .releaseDate(LocalDate.of(3000, 1, 1)).build();
        secondFilm = filmService.addFilm(secondFilm);
        Film thirdFilm = getNewFilledFilm().toBuilder().directors(Set.of(director))
                .releaseDate(LocalDate.of(2000, 1, 1)).build();
        thirdFilm = filmService.addFilm(thirdFilm);
        Film fourthFilm = getNewFilledFilm().toBuilder().directors(Set.of())
                .releaseDate(LocalDate.of(4000, 1, 1)).build();
        fourthFilm = filmService.addFilm(fourthFilm);
        final User firstUser = userService.addUser(TestUserUtils.getNewUser());
        final User secondUser = userService.addUser(TestUserUtils.getNewUser());

        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());

        assertThrows(ValidationException.class, () -> filmService.getSortedDirectorFilms(director.getId(), "unknown"));

        final Collection<Film> sortedByYearFilms = filmService.getSortedDirectorFilms(director.getId(), "year");

        assertEquals(3, sortedByYearFilms.size());
        assertEquals(firstFilm, sortedByYearFilms
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));
        assertEquals(thirdFilm, sortedByYearFilms
                .stream().skip(1).findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));

        final Collection<Film> sortedByYearLikes = filmService.getSortedDirectorFilms(director.getId(), "likes");

        assertEquals(3, sortedByYearLikes.size());
        assertEquals(secondFilm, sortedByYearLikes
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));
        assertEquals(firstFilm, sortedByYearLikes
                .stream().skip(1).findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));
    }

    @Test
    void testSearchFilms() {
        Director firstDirector = directorStorage.put(TestDirectorUtils.getNewDirector().toBuilder().name("AAAATi").build());
        Director secondDirector = directorStorage.put(TestDirectorUtils.getNewDirector().toBuilder().name("00000").build());
        Film firstFilm = filmService.addFilm(getNewFilledFilm().toBuilder().name("TiTle").directors(Set.of(secondDirector)).build());
        Film secondFilm = filmService.addFilm(getNewFilledFilm().toBuilder().name("000").directors(Set.of(secondDirector)).build());
        Film thirdFilm = filmService.addFilm(getNewFilledFilm().toBuilder().directors(Set.of(firstDirector)).build());
        User firstUser = userService.addUser(TestUserUtils.getNewUser());
        User secondUser = userService.addUser(TestUserUtils.getNewUser());

        filmService.addLike(thirdFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());

        Collection<Film> searchByDirector = filmService.searchFilms("AaA", "director");

        assertTrue(searchByDirector.contains(thirdFilm));
        assertEquals(1, searchByDirector.size());

        Collection<Film> searchByTitle = filmService.searchFilms("ti", "title");

        assertTrue(searchByTitle.contains(firstFilm));
        assertEquals(1, searchByDirector.size());

        Collection<Film> searchByTitleAndDirector = filmService.searchFilms("ti", "title,director");

        assertTrue(searchByTitleAndDirector.contains(firstFilm));
        assertTrue(searchByTitleAndDirector.contains(thirdFilm));
        assertEquals(2, searchByTitleAndDirector.size());
        assertEquals(thirdFilm, searchByTitleAndDirector.stream()
                .findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));
        assertEquals(firstFilm, searchByTitleAndDirector.stream()
                .skip(1).findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));

        Collection<Film> searchCrossedFilms = filmService.searchFilms("000", "title,director");

        assertTrue(searchCrossedFilms.contains(secondFilm));
        assertTrue(searchCrossedFilms.contains(firstFilm));
        assertEquals(2, searchCrossedFilms.size());
        assertEquals(secondFilm, searchCrossedFilms.stream()
                .findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));
        assertEquals(firstFilm, searchCrossedFilms.stream()
                .skip(1).findFirst().orElseThrow(() -> new RuntimeException("Фильм не найден")));

        assertEquals(0, filmService.searchFilms("xxxxxx", "title,director").size());
        assertEquals(0, filmService.searchFilms("xxxxxx", "director").size());
        assertEquals(0, filmService.searchFilms("xxxxxx", "title").size());
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("xxxxxx", "").size());
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("gdrgsge", "grdsg, htfthfdht"));
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("gdrgsge", ",,,"));
        assertDoesNotThrow(() -> filmService.searchFilms("", "title"));
    }

    @Test
    public void testGetPopularFilteredByGenreAndDirector() {
        Genre genre1 = TestGenreUtils.getNewGenre(1L);
        Genre genre2 = TestGenreUtils.getNewGenre(2L);

        Film film1 = TestFilmUtils.getNewFilmWithGenreAndYear(genre1, "2015");
        Film film2 = TestFilmUtils.getNewFilmWithGenreAndYear(genre2, "2010");
        Film film3 = TestFilmUtils.getNewFilmWithGenreAndYear(genre1, "2010");
        Film film4 = TestFilmUtils.getNewFilmWithGenreAndYear(genre2, "2015");

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);
        filmService.addFilm(film4);

        final Collection<Film> filteredByGenre =
                filmService.getPopularByYearAndGenre(10, genre1.getId(), null);
        final Collection<Film> filteredByDate =
                filmService.getPopularByYearAndGenre(10, null, "2010");
        final Collection<Film> filteredByGenreAndDate =
                filmService.getPopularByYearAndGenre(10, genre2.getId(), "2015");
        final Collection<Film> withoutFilter =
                filmService.getPopularByYearAndGenre(10, null, null);

        // Проверка filteredByGenre
        assertEquals(2, filteredByGenre.size());
        assertEquals(film1.getName(), filteredByGenre.stream().findFirst().get().getName());
        assertEquals(film3.getName(), filteredByGenre.stream().skip(1).findFirst().get().getName());

        // Проверка filteredByDate
        assertEquals(2, filteredByDate.size());
        assertEquals(film2.getName(), filteredByDate.stream().findFirst().get().getName());
        assertEquals(film3.getName(), filteredByDate.stream().skip(1).findFirst().get().getName());

        // Проверка filteredByGenreAndDate
        assertEquals(1, filteredByGenreAndDate.size());
        assertEquals(film4.getName(), filteredByGenreAndDate.stream().findFirst().get().getName());

        // Проверка withoutFilter
        assertEquals(4, withoutFilter.size());
        assertEquals(film1.getName(), withoutFilter.stream().findFirst().get().getName());
        assertEquals(film2.getName(), withoutFilter.stream().skip(1).findFirst().get().getName());
        assertEquals(film3.getName(), withoutFilter.stream().skip(2).findFirst().get().getName());
        assertEquals(film4.getName(), withoutFilter.stream().skip(3).findFirst().get().getName());
    }
}