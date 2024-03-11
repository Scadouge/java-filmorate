package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "users")
public class UserController {
    private final UserService userService;

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        log.info("Добавление пользователя {}", user);
        return userService.addItem(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Обновление пользователя {}", user);
        return userService.updateItem(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        log.info("Получение пользователя id={}", id);
        return userService.getItem(Long.valueOf(id));
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Получение списка всех пользователей");
        return userService.getAllItems();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление в список друзей id={}, friendId={}", id, friendId);
        return userService.updateFriends(id, friendId, false);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление из списока друзей id={}, friendId={}", id, friendId);
        return userService.updateFriends(id, friendId, true);
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
}
