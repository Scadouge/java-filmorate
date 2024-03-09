package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class FilmService extends AbstractService<FilmStorage, Film> {
    public FilmService(FilmStorage storage) {
        super(storage);
    }

    public Film updateLikes(Long id, Long userId, boolean isDeletion) {
        Film film = getItemOrThrow(id);
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
        return storage.getPopular(count);
    }
}
