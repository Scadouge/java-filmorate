package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilmStorage extends CrudStorage<Film> {
    void addMark(Film film, User user, Integer rating);

    void removeMark(Film film, User user);

    Collection<Film> getPopularByYearAndGenre(Integer count, Long genreId, String year);

    Collection<Film> getFavouriteFilms(User user);

    Collection<Film> getSortedDirectorFilms(Director director, String sortBy);

    Collection<Film> searchFilms(String query, String by);

    Map<Long, List<Film>> getLikedFilms();
}
