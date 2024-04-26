package ru.yandex.practicum.filmorate.dao;

import java.util.Collection;

public interface CrudStorage<T> {
    T put(T item);

    T get(Long id);

    Collection<T> getAll();

    T update(T item);

    T delete(T item);
}
