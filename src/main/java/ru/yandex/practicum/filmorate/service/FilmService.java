package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;
    private final EventService eventService;

    public Film addFilm(Film film) {
        log.debug("Добавление фильма film={}", film);
        return filmStorage.put(film);
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма film={}", film);
        if (film.getId() == null) {
            throw new ValidationException("Не указан id фильма");
        }
        filmStorage.get(film.getId());
        return filmStorage.update(film);
    }

    public Film getFilm(Long id) {
        log.debug("Получение фильма id={}", id);
        return filmStorage.get(id);
    }

    public Collection<Film> getAllFilms() {
        log.debug("Получение списка всех фильмов");
        return filmStorage.getAll();
    }

    public void addMark(Long filmId, Long userId, Integer rating) {
        log.debug("Добавление оценки id={}, userId={}, rating={}", filmId, userId, rating);
        boolean success = filmStorage.addMark(filmStorage.get(filmId), userStorage.get(userId), rating);
        if (success) {
            eventService.createAddMarkEvent(userId, filmId);
        }
    }

    public void removeMark(Long filmId, Long userId) {
        log.debug("Удаление оценки id={}, userId={}", filmId, userId);
        boolean success = filmStorage.removeMark(filmStorage.get(filmId), userStorage.get(userId));
        if (success) {
            eventService.createRemoveMarkEvent(userId, filmId);
        }
    }

    public Collection<Film> getPopularByYearAndGenre(Integer count, Long genreId, String year) {
        log.debug("Получение списка популярных фильмов count={} с фильтрацией по genreId={} и year={}",
                count, genreId, year);
        return filmStorage.getPopularByYearAndGenre(count, genreId, year);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение списка общих фильмов пользователей с userId={} и friendId={}", userId, friendId);
        return filmStorage.getCommonFilms(userStorage.get(userId), userStorage.get(friendId));
    }

    public Film deleteFilm(Long filmId) {
        log.info("Удаление фильма с id={}", filmId);
        Film film = getFilm(filmId);
        return filmStorage.delete(film);
    }

    public Collection<Film> getSortedDirectorFilms(Long directorId, String sortBy) {
        log.debug("Получение списка фильмов режиссера directorId={}, sortBy={}", directorId, sortBy);
        return filmStorage.getSortedDirectorFilms(directorStorage.get(directorId), sortBy);
    }

    public Collection<Film> searchFilms(String query, String by) {
        log.debug("Получение списка фильмов по поисковому запросу query={}, by={}", query, by);
        return filmStorage.searchFilms(query, by);
    }

    public Map<Long, HashMap<Film, Integer>> getLikedFilms() {
        log.debug("Получение списка понравившихся фильмов для каждого пользователя");
        return filmStorage.getLikedFilms();
    }
}
