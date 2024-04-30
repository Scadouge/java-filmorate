package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User user) {
        log.info("Добавление пользователя user={}", user);
        return userStorage.put(user);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя id={}, user={}", user.getId(), user);
        if (user.getId() == null) {
            throw new ValidationException("Не указан id пользователя");
        }
        userStorage.get(user.getId());
        return userStorage.update(user);
    }

    public User getUser(Long id) {
        log.info("Получение пользователя id={}", id);
        return userStorage.get(id);
    }

    public Collection<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.getAll();
    }

    public User addFriend(Long userId, Long friendId) {
        log.info("Отправка запроса в друзья userId={}, friendId={}", userId, friendId);
        User user = getUser(userId);
        User friend = getUser(friendId);
        userStorage.addFriend(user, friend);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.info("Удаление из друзей userId={}, friendId={}", userId, friendId);
        User user = getUser(userId);
        User friend = getUser(friendId);
        userStorage.removeFriend(user, friend);
        return user;
    }

    public Collection<User> getFriends(Long id) {
        log.info("Получение списка друзей id={}", id);
        return userStorage.getFriends(getUser(id));
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей id={}, otherId={}", id, otherId);
        Collection<User> userFriends = userStorage.getFriends(getUser(id));
        Collection<User> friendFriends = userStorage.getFriends(getUser(otherId));
        userFriends.retainAll(friendFriends);
        return userFriends;
    }

    public User deleteUser(Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        User user = getUser(userId);
        return userStorage.delete(user);
    }
}
