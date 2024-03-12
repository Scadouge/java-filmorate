package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService extends AbstractService<FilmStorage, Film> {
    private static final Comparator<Film> FILM_COMPARATOR_SORT_BY_LIKES = Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder());

    public FilmService(FilmStorage storage) {
        super(storage);
    }

    public Film updateLikes(Long id, Long userId, boolean isDeletion) {
        log.info("Обновление лайков id={}, userId={}, isDeletion={}", id, userId, isDeletion);
        Film film = getItem(id);
        Film updatedFilm = updateFilmLikes(film, userId, isDeletion);
        storage.put(updatedFilm);
        return updatedFilm;
    }

    private Film updateFilmLikes(Film film, Long userId, boolean isDeletion) {
        Set<Long> likes = new HashSet<>(film.getLikes());
        if (isDeletion) {
            likes.remove(userId);
        } else {
            likes.add(userId);
        }
        return film.toBuilder().likes(likes).build();
    }

    public Collection<Film> getPopular(int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        return storage.getAll().stream()
                .sorted(FILM_COMPARATOR_SORT_BY_LIKES)
                .limit(count)
                .collect(Collectors.toList());
    }
}
