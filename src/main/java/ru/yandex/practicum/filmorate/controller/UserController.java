package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Добавление пользователя {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Обновление пользователя {}", user);
        if (user.getId() == null) {
            throw new ValidationException("Не указан id пользователя");
        }
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получение пользователя id={}", id);
        return userService.getUser(id);
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление в список друзей id={}, friendId={}", id, friendId);
        if (Objects.equals(id, friendId)) {
            throw new ValidationException("При добавлении в друзья id пользователей не должны быть равны");
        }
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление из списока друзей id={}, friendId={}", id, friendId);
        if (Objects.equals(id, friendId)) {
            throw new ValidationException("При удалении из друзей id пользователей не должны быть равны");
        }
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Получение списка друзей id={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получение списка общих друзей id={}, otherId={}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/{userId}")
    public User deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        return userService.deleteUser(userId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable Long id) {
        log.info("Получение списка рекомендаций для пользователя id={}", id);
        return userService.getRecommendedFilms(id);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getFeed(@PathVariable Long id) {
        log.info("Получение списка событий для пользователя id={}", id);
        return userService.getFeed(id);
    }
}
