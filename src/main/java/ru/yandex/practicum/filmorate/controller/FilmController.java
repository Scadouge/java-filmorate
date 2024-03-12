package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping()
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма {}", film);
        return filmService.addItem(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма {}", film);
        return filmService.updateItem(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Получение фильма id={}", id);
        return filmService.getItem(id);
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Получение списка всех фильмов");
        return filmService.getAllItems();
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка id={}, userId={}", id, userId);
        return filmService.updateLikes(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка id={}, userId={}", id, userId);
        return filmService.updateLikes(id, userId, true);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        return filmService.getPopular(count);
    }
}
