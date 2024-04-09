package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Component
public class InMemoryUserStorage implements UserStorage {
    // not implemented
    @Override
    public User put(User item) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User item) {
        return null;
    }

    @Override
    public User delete(User item) {
        return null;
    }

    @Override
    public Collection<User> getAll() {
        return null;
    }

    @Override
    public void addFriend(User user, User friend) {

    }

    @Override
    public void removeFriend(User user, User friend) {

    }

    @Override
    public Collection<User> getFriends(User user) {
        return null;
    }
}
