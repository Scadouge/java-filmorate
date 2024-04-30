package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FilmStorage extends CrudStorage<Film> {
    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    Collection<Film> getPopularByLikes(int max);

    int getLikesCount(Film film);

    Collection<Film> getSortedDirectorFilms(Director director, String sortBy);

    Collection<Film> searchFilms(String query, String by);
}
