package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "films")
public class FilmController extends AbstractController<Film> {

    @PostMapping()
    public Film addFilm(@Valid @RequestBody Film film) {
        int id = getValidatedOrGenerateId(film.getId());
        film = film.toBuilder().id(id).build();
        map.put(id, film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        Integer id = getValidatedExistingId(film.getId());
        film = film.toBuilder().id(id).build();
        map.put(id, film);
        log.info("Обновлен фильм {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getMap() {
        return map.values();
    }
}
