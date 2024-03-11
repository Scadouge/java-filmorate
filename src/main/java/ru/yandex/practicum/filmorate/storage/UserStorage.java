package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage extends Storage<User> {
    Collection<User> getFriends(Long id);
}
