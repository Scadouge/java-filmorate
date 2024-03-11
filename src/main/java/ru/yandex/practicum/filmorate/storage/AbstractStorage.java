package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.StorageItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public abstract class AbstractStorage<T extends StorageItem> implements Storage<T> {
    protected long generatedId = 1;
    protected final Map<Long, T> storage = new HashMap<>();

    @Override
    public T put(T item) {
        Long id = item.getId();
        if (id == null) {
            id = generateId();
            item = (T) item.toBuilder().id(id).build();
        }
        log.info("Добавление предмета в хранилище id={}, item={}", id, item);
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public T get(Long id) {
        log.info("Получение предмета из хранилища id={}", id);
        return storage.get(id);
    }

    @Override
    public Collection<T> getAll() {
        log.info("Добавление всех предметов из хранилища");
        return storage.values();
    }

    @Override
    public T delete(Long id) {
        log.info("Удаление предмета из хранилища id={}", id);
        return storage.remove(id);
    }

    @Override
    public boolean isContains(Long id) {
        log.info("Проверка содержания ключа в хранилище id={}", id);
        return storage.containsKey(id);
    }

    protected long generateId() {
        return generatedId++;
    }
}
