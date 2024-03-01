package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractController<T> {

    protected int generatedId = 1;
    protected final Map<Integer, T> map = new HashMap<>();

    protected int getValidatedOrGenerateId(Integer id) {
        if (id == null) {
            id = generateId();
        } else {
            validateIdPost(id);
        }
        return id;
    }

    protected int getValidatedExistingId(Integer id) {
        validateIdPut(id);
        return id;
    }

    private void validateIdPost(Integer id) {
        if (map.containsKey(id)) {
            ValidationException validationException = new ValidationException("Объект с таким id уже существует");
            log.warn("Валидация id провалена", validationException);
            throw validationException;
        }
    }

    private void validateIdPut(Integer id) {
        if (id == null || !map.containsKey(id)) {
            ValidationException validationException = new ValidationException("Объект с таким id не существует");
            log.warn("Валидация id провалена", validationException);
            throw validationException;
        }
    }

    private int generateId() {
        return generatedId++;
    }
}
