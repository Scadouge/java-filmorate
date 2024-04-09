package ru.yandex.practicum.filmorate.exception;

public class DbStorageException extends RuntimeException {
    public DbStorageException(String message) {
        super(message);
    }
}
