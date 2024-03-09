package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.StorageItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public abstract class AbstractStorage<T extends StorageItem> implements Storage<T> {
    protected long generatedId = 1;
    protected final Map<Long, T> storage = new HashMap<>();

    @Override
    public T put(T item) {
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public T get(Long id) {
        return storage.get(id);
    }

    @Override
    public Collection<T> getAll() {
        return storage.values();
    }

    @Override
    public T delete(Long id) {
        return storage.remove(id);
    }

    @Override
    public boolean isContains(Long id) {
        return storage.containsKey(id);
    }

    protected long generateId() {
        return generatedId++;
    }
}
