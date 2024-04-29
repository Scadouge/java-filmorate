package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Slf4j
@AllArgsConstructor
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        log.info("Добавление режиссера director={}", director);
        return directorStorage.put(director);
    }

    public Director updateDirector(Director director) {
        log.info("Обновление режиссера id={}, director={}", director.getId(), director);
        if (director.getId() == null) {
            throw new ValidationException("Не указан id режиссера");
        }
        directorStorage.get(director.getId());
        return directorStorage.update(director);
    }

    public Director getDirector(Long id) {
        log.info("Получение режиссера id={}", id);
        return directorStorage.get(id);
    }

    public Collection<Director> getAllDirectors() {
        log.info("Получение списка всех режиссеров");
        return directorStorage.getAll();
    }

    public Director deleteDirector(Long directorId) {
        log.info("Удаление режиссера с id={}", directorId);
        Director director = getDirector(directorId);
        return directorStorage.delete(director);
    }
}
