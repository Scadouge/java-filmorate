package ru.yandex.practicum.filmorate.exception;

public class ItemAlreadyExistException extends RuntimeException {
    private final Long id;

    public ItemAlreadyExistException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
