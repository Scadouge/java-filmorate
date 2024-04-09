package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    // not implemented
    @Override
    public Film put(Film item) {
        return null;
    }

    // not implemented
    @Override
    public Film get(Long id) {
        return null;
    }

    // not implemented
    @Override
    public Film update(Film item) {
        return null;
    }

    // not implemented
    @Override
    public Film delete(Film film) {
        return null;
    }

    // not implemented
    @Override
    public Collection<Film> getAll() {
        return null;
    }

    // not implemented
    @Override
    public void addLike(Film film, User user) {
    }

    // not implemented
    @Override
    public void removeLike(Film film, User user) {
    }

    // not implemented
    @Override
    public Collection<Film> getPopularByLikes(int max) {
        return null;
    }

    // not implemented
    @Override
    public int getLikes(Film film) {
        return 0;
    }
}
