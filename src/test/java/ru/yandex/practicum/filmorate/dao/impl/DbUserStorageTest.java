package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestUserUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.FRIENDSHIP_STATUS;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.FRIENDSHIP_USER_ID;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.FRIENDSHIP;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbUserStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    @Test
    public void testFriends() {
        final User firstUser = userStorage.put(TestUserUtils.getNewUser());
        final User secondUser = userStorage.put(TestUserUtils.getNewUser());

        assertEquals(firstUser.getName(), firstUser.getLogin());
        assertEquals(0, userStorage.getFriends(firstUser).size());

        userStorage.addFriend(firstUser, secondUser);
        userStorage.addFriend(firstUser, secondUser);
        userStorage.addFriend(firstUser, secondUser);
        userStorage.addFriend(firstUser, secondUser);
        final Collection<User> friendListWithOneFriend = userStorage.getFriends(firstUser);
        final FriendshipStatus firstToSecondFriendshipStatus =
                FriendshipStatus.valueOf(Objects.requireNonNull(jdbcTemplate.queryForObject(new SqlHelper()
                                .select(FRIENDSHIP_STATUS).from(FRIENDSHIP)
                                .where(FRIENDSHIP_USER_ID, List.of(firstUser.getId())).toString(),
                        (rs, RowNum) -> (rs.getString(FRIENDSHIP_STATUS.name())))));

        assertEquals(1, friendListWithOneFriend.size());
        assertTrue(friendListWithOneFriend.contains(secondUser));
        assertFalse(userStorage.getFriends(secondUser).contains(firstUser));
        assertEquals(FriendshipStatus.UNACCEPTED, firstToSecondFriendshipStatus);

        userStorage.addFriend(secondUser, firstUser);
        final FriendshipStatus firstToSecondFriendshipStatusAgain =
                FriendshipStatus.valueOf(Objects.requireNonNull(jdbcTemplate.queryForObject(new SqlHelper()
                                .select(FRIENDSHIP_STATUS).from(FRIENDSHIP)
                                .where(FRIENDSHIP_USER_ID, List.of(firstUser.getId())).toString(),
                        (rs, RowNum) -> (rs.getString(FRIENDSHIP_STATUS.name())))));
        final FriendshipStatus secondToFirstFriendshipStatus =
                FriendshipStatus.valueOf(Objects.requireNonNull(jdbcTemplate.queryForObject(new SqlHelper()
                                .select(FRIENDSHIP_STATUS).from(FRIENDSHIP)
                                .where(FRIENDSHIP_USER_ID, List.of(secondUser.getId())).toString(),
                        (rs, RowNum) -> (rs.getString(FRIENDSHIP_STATUS.name())))));

        assertEquals(FriendshipStatus.ACCEPTED, firstToSecondFriendshipStatusAgain);
        assertEquals(FriendshipStatus.ACCEPTED, secondToFirstFriendshipStatus);

        userStorage.removeFriend(firstUser, secondUser);
        final Collection<User> friendListWithoutFriends = userStorage.getFriends(firstUser);
        final FriendshipStatus secondToFirstFriendshipStatusAgain =
                FriendshipStatus.valueOf(Objects.requireNonNull(jdbcTemplate.queryForObject(new SqlHelper()
                                .select(FRIENDSHIP_STATUS).from(FRIENDSHIP)
                                .where(FRIENDSHIP_USER_ID, List.of(secondUser.getId())).toString(),
                        (rs, RowNum) -> (rs.getString(FRIENDSHIP_STATUS.name())))));

        assertThrows(EmptyResultDataAccessException.class,
                () -> FriendshipStatus.valueOf(Objects.requireNonNull(jdbcTemplate.queryForObject(new SqlHelper()
                                .select(FRIENDSHIP_STATUS).from(FRIENDSHIP)
                                .where(FRIENDSHIP_USER_ID, List.of(firstUser.getId())).toString(),
                        (rs, RowNum) -> (rs.getString(FRIENDSHIP_STATUS.name()))))));
        assertEquals(0, friendListWithoutFriends.size());
        assertEquals(FriendshipStatus.UNACCEPTED, secondToFirstFriendshipStatusAgain);

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