package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestUserUtils;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbUserStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new DbUserStorage(jdbcTemplate);
    }

    @Test
    public void testFriends() {
        final User firstUser = userStorage.put(TestUserUtils.getNewUser());
        final User secondUser = userStorage.put(TestUserUtils.getNewUser());

        assertEquals(firstUser.getName(), firstUser.getLogin());
        assertEquals(0, userStorage.getFriends(firstUser).size());

        userStorage.addFriend(firstUser, secondUser);
        final Collection<User> collectionWithOneFriend = userStorage.getFriends(firstUser);

        assertEquals(1, collectionWithOneFriend.size());
        assertTrue(collectionWithOneFriend.contains(secondUser));
        assertFalse(userStorage.getFriends(secondUser).contains(firstUser));

        userStorage.removeFriend(firstUser, secondUser);
        final Collection<User> collectionWithoutFriend = userStorage.getFriends(firstUser);

        assertEquals(0, collectionWithoutFriend.size());

        final User nonExistingUser = TestUserUtils.getNewUser();

        assertDoesNotThrow(() -> userStorage.removeFriend(firstUser, nonExistingUser));
        assertDoesNotThrow(() -> userStorage.removeFriend(nonExistingUser, firstUser));
    }

    @Test
    public void testGetUserById() {
        final User newUser = userStorage.put(TestUserUtils.getNewUser());
        final User savedUser = userStorage.get(newUser.getId());

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
        assertThrows(ItemNotFoundException.class, () -> userStorage.get(TestUserUtils.getNewNonExistentUser().getId()));
    }

    @Test
    public void testGetAllUsers() {
        User firstNewUser = userStorage.put(TestUserUtils.getNewUser());
        User secondNewUser = userStorage.put(TestUserUtils.getNewUser());

        Collection<User> savedUsers = userStorage.getAll();
        assertTrue(savedUsers.contains(firstNewUser));
        assertTrue(savedUsers.contains(secondNewUser));
    }

    @Test
    public void testUpdateUser() {
        final User newUser = userStorage.put(TestUserUtils.getNewUser());
        final User toUpdateUser = TestUserUtils.getNewUser(newUser.getId());
        final User savedUser = userStorage.update(toUpdateUser);

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateUser);

        final User updatedUserWrongId = TestUserUtils.getNewNonExistentUser();

        assertThrows(ItemNotFoundException.class, () -> userStorage.update(updatedUserWrongId));
    }

    @Test
    public void testDeleteUser() {
        User newUser = userStorage.put(TestUserUtils.getNewUser());
        User secondUser = userStorage.put(TestUserUtils.getNewUser());

        userStorage.addFriend(newUser, secondUser);

        assertEquals(1, userStorage.getFriends(newUser).size());

        userStorage.delete(secondUser);

        assertEquals(0, userStorage.getFriends(newUser).size());

        userStorage.delete(newUser);

        assertThrows(ItemNotFoundException.class, () -> userStorage.get(newUser.getId()));
        assertDoesNotThrow(() -> userStorage.delete(TestUserUtils.getNewNonExistentUser()));
    }
}