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

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

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

    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка filmId={}, userId={}", filmId, userId);
        filmStorage.addLike(filmStorage.get(filmId), userStorage.get(userId));
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка filmId={}, userId={}", filmId, userId);
        filmStorage.removeLike(filmStorage.get(filmId), userStorage.get(userId));
    }

    public Collection<Film> getPopular(int count) {
        log.debug("Получение списка популярных фильмов count={}", count);
        return filmStorage.getPopularByLikes(count);
    }

    public Collection<Film> getSortedDirectorFilms(Long directorId, String sortBy) {
        log.debug("Получение списка фильмов режиссера directorId={}, sortBy={}", directorId, sortBy);
        return filmStorage.getSortedDirectorFilms(directorStorage.get(directorId), sortBy);
    }

    public Collection<Film> searchFilms(String query, String by) {
        log.debug("Получение списка фильмов по поисковому запросу query={}, by={}", query, by);
        return filmStorage.searchFilms(query, by);
    }
}
