package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма {}", film);
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Получение фильма id={}", id);
        return filmService.getFilm(id);
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Получение списка всех фильмов");
        return filmService.getAllFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка id={}, userId={}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка id={}, userId={}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        return filmService.getPopular(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getSortedDirectorFilms(@PathVariable Long directorId, @RequestParam String sortBy) {
        log.info("Получение списка фильмов режиссера directorId={}, sortBy={}", directorId, sortBy);
        return filmService.getSortedDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        log.info("Получение списка фильмов по поисковому запросу query={}, by={}", query, by);
        if (query.isBlank()) {
            throw new ValidationException("Параметр query не может быть пустым");
        }
        return filmService.searchFilms(query, by);
    }
}
