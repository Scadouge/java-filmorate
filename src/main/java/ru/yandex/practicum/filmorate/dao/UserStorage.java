package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage extends CrudStorage<User> {
    void addFriend(User user, User friend);

    void removeFriend(User user, User friend);

    Collection<User> getFriends(User user);
}
