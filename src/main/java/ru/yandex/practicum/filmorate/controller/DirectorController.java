package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Добавление режиссера {}", director);
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Обновление режиссера {}", director);
        if (director.getId() == null) {
            throw new ValidationException("Не указан id режиссера");
        }
        return directorService.updateDirector(director);
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable Long id) {
        log.info("Получение режиссера id={}", id);
        return directorService.getDirector(id);
    }

    @GetMapping
    public Collection<Director> getAll() {
        log.info("Получение списка всех режиссеров");
        return directorService.getAllDirectors();
    }

    @DeleteMapping("/{directorId}")
    public Director deleteDirector(@PathVariable Long directorId) {
        log.info("Удаление режиссера с id={}", directorId);
        return directorService.deleteDirector(directorId);
    }
}
