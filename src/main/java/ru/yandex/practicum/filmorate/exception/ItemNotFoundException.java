package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public class ItemNotFoundException extends RuntimeException {
    private final Long id;

    public ItemNotFoundException(Long id) {
        this.id = id;
    }

    public ItemNotFoundException(Long id, String message) {
        super(message);
        this.id = id;
    }
}
