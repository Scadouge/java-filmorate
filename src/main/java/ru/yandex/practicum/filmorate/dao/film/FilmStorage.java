package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface FilmStorage extends CrudStorage<Film> {
    boolean addMark(Film film, User user, Integer rating);

    boolean removeMark(Film film, User user);

    void deleteAllUserLikesMarksFilms(User user);

    Collection<Film> getPopularByYearAndGenre(Integer count, Long genreId, String year);

    Collection<Film> getCommonFilms(User user, User friend);

    Collection<Film> getSortedDirectorFilms(Director director, String sortBy);

    Collection<Film> searchFilms(String query, String by);

    Map<Long, HashMap<Film, Integer>> getLikedFilms();
}
