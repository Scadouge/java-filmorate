package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class FilmService extends AbstractService<FilmStorage, Film> {
    public FilmService(FilmStorage storage) {
        super(storage);
    }

    public Film updateLikes(Long id, Long userId, boolean isDeletion) {
        log.info("Обновление лайков id={}, userId={}, isDeletion={}", id, userId, isDeletion);
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
<<<<<<< Updated upstream
        return storage.getPopular(count);
=======
        log.info("Получение списка популярных фильмов count={}", count);
        return storage.getAll().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
>>>>>>> Stashed changes
    }
}
