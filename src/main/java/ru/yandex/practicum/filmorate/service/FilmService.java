package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {
        log.info("Добавление фильма film={}", film);
        return filmStorage.put(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма id={}, film={}", film.getId(), film);
        if (film.getId() == null) {
            throw new ValidationException("Не указан id фильма");
        }
        return filmStorage.update(film);
    }

    public Film getFilm(Long id) {
        log.info("Получение фильма id={}", id);
        return filmStorage.get(id);
    }

    public Collection<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return filmStorage.getAll();
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка filmId={}, userId={}", filmId, userId);
        filmStorage.addLike(filmStorage.get(filmId), userStorage.get(userId));
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка filmId={}, userId={}", filmId, userId);
        filmStorage.removeLike(filmStorage.get(filmId), userStorage.get(userId));
    }

    public Collection<Film> getPopular(int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        return filmStorage.getPopularByLikes(count);
    }
}
