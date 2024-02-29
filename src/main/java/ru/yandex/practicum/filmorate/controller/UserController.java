package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "users")
public class UserController extends AbstractController<User> {

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        int id = getValidatedOrGenerateId(user.getId());
        user = user.toBuilder().id(id).name(getNameOrUseLogin(user)).build();
        map.put(id, user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @PutMapping
    public User updateFilm(@Valid @RequestBody User user) {
        Integer id = getValidatedExistingId(user.getId());
        user = user.toBuilder().id(id).name(getNameOrUseLogin(user)).build();
        map.put(id, user);
        log.info("Обновлен пользователь {}", user);
        return user;
    }

    @GetMapping
    public Collection<User> getMap() {
        return map.values();
    }

    private String getNameOrUseLogin(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            name = user.getLogin();
            log.info("Имя пользователя отсутствует, используем логин {}", name);
        }
        return name;
    }
}
