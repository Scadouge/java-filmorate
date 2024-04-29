package ru.yandex.practicum.filmorate.dao.genre;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface GenreStorage extends CrudStorage<Genre> {
    Collection<Genre> get(Set<Long> genreIds);
}
