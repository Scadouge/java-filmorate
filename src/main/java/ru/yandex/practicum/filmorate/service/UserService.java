package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.recommendations.SlopeOne;

import java.util.Collection;
import java.util.List;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FilmService filmService;
    private final EventService eventService;

    public User addUser(User user) {
        log.debug("Добавление пользователя user={}", user);
        return userStorage.put(user);
    }

    public User updateUser(User user) {
        log.debug("Обновление пользователя id={}, user={}", user.getId(), user);
        userStorage.get(user.getId());
        return userStorage.update(user);
    }

    public User getUser(Long id) {
        log.debug("Получение пользователя id={}", id);
        return userStorage.get(id);
    }

    public Collection<User> getAllUsers() {
        log.debug("Получение списка всех пользователей");
        return userStorage.getAll();
    }

    public User addFriend(Long userId, Long friendId) {
        log.debug("Отправка запроса в друзья userId={}, friendId={}", userId, friendId);
        User user = getUser(userId);
        User friend = getUser(friendId);
        userStorage.addFriend(user, friend);
        eventService.createAddFriend(userId, friendId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.debug("Удаление из друзей userId={}, friendId={}", userId, friendId);
        User user = getUser(userId);
        User friend = getUser(friendId);
        userStorage.removeFriend(user, friend);
        eventService.createRemoveFriend(userId, friendId);
        return user;
    }

    public Collection<User> getFriends(Long id) {
        log.debug("Получение списка друзей id={}", id);
        return userStorage.getFriends(getUser(id));
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Получение списка общих друзей id={}, otherId={}", id, otherId);
        Collection<User> userFriends = userStorage.getFriends(getUser(id));
        Collection<User> friendFriends = userStorage.getFriends(getUser(otherId));
        userFriends.retainAll(friendFriends);
        return userFriends;
    }

    public User deleteUser(Long userId) {
        log.debug("Удаление пользователя с id={}", userId);
        User user = getUser(userId);
        return userStorage.delete(user);
    }

    public Collection<Film> getRecommendedFilms(Long id) {
        log.info("Получение рекомендаций фильмов для пользователя id={}", id);
        Map<Long, List<Film>> usersLikedFilms = filmService.getLikedFilms();
        return new SlopeOne(usersLikedFilms, id).slopeOne();
    }

    public List<Event> getFeed(Long id) {
        log.debug("Получение списка событий для пользователя с id={}", id);
        return eventService.findEventsByUserId(id);
    }
}
