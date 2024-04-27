package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Set;

public interface DirectorStorage extends CrudStorage<Director> {
    Collection<Director> get(Set<Long> directorIds);
}
