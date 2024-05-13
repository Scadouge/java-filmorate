package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.event.DbEventStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestUserUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbEventStorageTest {
    private final DbEventStorage eventStorage;
    private final UserStorage userStorage;

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