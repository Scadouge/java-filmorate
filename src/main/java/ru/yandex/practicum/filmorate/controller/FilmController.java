package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "films")
public class FilmController {

    private static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private int generated_id = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping()
    public Film addFilm(@Valid @RequestBody Film film) {
        Integer id = film.getId();
        if (id == null) {
            id = generateId();
        }
        validateIdPost(id);
        validateReleaseDate(film.getReleaseDate());

        film = film.toBuilder().id(id).build();
        films.put(id, film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        Integer id = film.getId();
        validateIdPut(id);
        validateReleaseDate(film.getReleaseDate());

        film = film.toBuilder().id(id).build();
        films.put(id, film);
        log.info("Обновлен фильм {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    public void validateIdPost(Integer id) {
        if (films.containsKey(id)) {
            ValidationException validationException = new ValidationException("Фильм с таким id уже существует");
            log.warn("Валидация id фильма провалена", validationException);
            throw validationException;
        }
    }

    public void validateIdPut(Integer id) {
        if (id == null || !films.containsKey(id)) {
            ValidationException validationException = new ValidationException("Фильм с таким id не существует");
            log.warn("Валидация id фильма провалена", validationException);
            throw validationException;
        }
    }

    public void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(MIN_FILM_RELEASE_DATE)) {
            ValidationException validationException = new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            log.warn("Валидация releaseDate фильма провалена", validationException);
            throw validationException;
        }
    }

    private int generateId() {
        return generated_id++;
    }
}
