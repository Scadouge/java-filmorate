package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ItemAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.StorageItem;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;

@Slf4j
public abstract class AbstractService<S extends Storage<T>, T extends StorageItem> {
    protected final S storage;

    public AbstractService(S storage) {
        this.storage = storage;
    }

    public T addItem(T item) {
        Long id = item.getId();
        log.info("Добавление предмета id={}, item={}", id, item);
        if (id != null && storage.isContains(id)) {
            throw new ItemAlreadyExistException(id);
        }
        return storage.put(item);
    }

    public T updateItem(T item) {
        Long id = item.getId();
        log.info("Обновление предмета id={}, item={}", id, item);
        if (id == null) {
            throw new ValidationException("Не указан id объекта");
        } else if (!storage.isContains(id)) {
            throw new ItemNotFoundException(id);
        }
        return storage.put(item);
    }

    public T getItem(Long id) {
        log.info("Получение предмета id={}", id);
        T item = storage.get(id);
        if (item == null) {
            throw new ItemNotFoundException(id);
        }
        return item;
    }

    public Collection<T> getAllItems() {
        log.info("Получение списка всех предметов");
        return storage.getAll();
    }

    protected T getItemOrThrow(Long id) {
        T film = storage.get(id);
        if (film == null) {
            throw new ItemNotFoundException(id);
        }
        return film;
    }
}
