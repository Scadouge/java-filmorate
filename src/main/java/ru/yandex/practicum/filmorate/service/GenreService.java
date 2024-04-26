package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@AllArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> getAllGenres() {
        log.info("Получение всех жанров");
        return genreStorage.getAll();
    }

    public Genre getGenre(Long id) {
        log.info("Получение жанра id={}", id);
        return genreStorage.get(id);
    }
}
