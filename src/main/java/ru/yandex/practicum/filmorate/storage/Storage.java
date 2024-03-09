package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.StorageItem;

import java.util.Collection;

public interface Storage<T extends StorageItem> {
    T put(T item);

    T get(Long id);

    Collection<T> getAll();

    T delete(Long id);

    boolean isContains(Long id);
}
