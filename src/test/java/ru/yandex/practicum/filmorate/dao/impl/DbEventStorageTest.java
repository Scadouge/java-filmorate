package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.event.DbEventStorage;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestUserUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbEventStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private DbEventStorage eventStorage;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        eventStorage = new DbEventStorage(jdbcTemplate);
        userStorage = new DbUserStorage(jdbcTemplate);
    }

    @Test
    void saveOne() {
        final User firstUser = userStorage.put(TestUserUtils.getNewUser());
        final User secondUser = userStorage.put(TestUserUtils.getNewUser());
        userStorage.addFriend(firstUser, secondUser);
        assertTrue(eventStorage.findAllById(firstUser.getId()).toString().length() > 0);
    }

    @Test
    void findAllById() {
        final User firstUser = userStorage.put(TestUserUtils.getNewUser());
        final User secondUser = userStorage.put(TestUserUtils.getNewUser());
        userStorage.addFriend(firstUser, secondUser);
        userStorage.removeFriend(firstUser, secondUser);
        assertTrue(eventStorage.findAllById(firstUser.getId()).toString().length() > 0);
    }
}