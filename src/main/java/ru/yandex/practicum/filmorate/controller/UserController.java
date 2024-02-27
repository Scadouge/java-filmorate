package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "users")
public class UserController {
    private int generated_id = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        Integer id = user.getId();
        if (id == null) {
            id = generateId();
        }
        validateIdPost(id);
        validateLogin(user.getLogin());

        user = user.toBuilder().id(id).name(getNameOrUseLogin(user)).build();
        users.put(id, user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @PutMapping
    public User updateFilm(@Valid @RequestBody User user) {
        Integer id = user.getId();

        validateIdPut(id);
        validateLogin(user.getLogin());

        user = user.toBuilder().id(id).name(getNameOrUseLogin(user)).build();
        users.put(id, user);
        log.info("Обновлен пользователь {}", user);
        return user;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    public void validateIdPost(Integer id) {
        if (users.containsKey(id)) {
            ValidationException validationException = new ValidationException("Пользователь с таким id уже существует");
            log.warn("Валидация id пользователя провалена", validationException);
            throw validationException;
        }
    }

    public void validateIdPut(Integer id) {
        if (id == null || !users.containsKey(id)) {
            ValidationException validationException = new ValidationException("Пользователь с таким id не существует");
            log.warn("Валидация id пользователя провалена", validationException);
            throw validationException;
        }
    }

    public void validateLogin(String login) {
        if (login.contains(" ")) {
            ValidationException validationException = new ValidationException("Логин не должен содержать пробелов");
            log.warn("Валидация login пользователя провалена", validationException);
            throw validationException;
        }
    }

    private String getNameOrUseLogin(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            name = user.getLogin();
            log.info("Имя пользователя отсутствует, используем логин {}", name);
        }
        return name;
    }

    private int generateId() {
        return generated_id++;
    }
}
