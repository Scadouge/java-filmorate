package ru.yandex.practicum.filmorate.dao.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.USERS;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;

    @Override
    public User put(User user) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(USERS.name());
        jdbcInsert.usingGeneratedKeyColumns(USER_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(USER_NAME.name(), user.getName());
        params.put(USER_LOGIN.name(), user.getLogin());
        params.put(USER_EMAIL.name(), user.getEmail());
        params.put(USER_BIRTHDAY.name(), Date.valueOf(user.getBirthday()));
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        User updatedUser = user.toBuilder().id(id).build();
        log.info("Добавление пользователя user={}", updatedUser);
        return updatedUser;
    }

    @Override
    public User get(Long id) {
        log.debug("Получение пользователя id={}", id);
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> UserMapper.createUser(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<User> getAll() {
        log.debug("Получение всех пользователей");
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> UserMapper.createUser(rs));
    }

    @Override
    public User update(User user) {
        log.debug("Обновление пользователя user={}", user);
        String sql = "UPDATE users SET user_name = ?, user_login = ?, user_email = ?, user_birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getEmail(), user.getBirthday(), user.getId());
        return get(user.getId());
    }

    @Override
    public User delete(User user) {
        log.debug("Удаление пользователя id={}", user.getId());
        filmStorage.deleteAllUserLikesFromFilms(user);
        reviewStorage.deleteAllUserScoresFromReviews(user);
        SqlHelper helper = new SqlHelper();
        helper.delete(USERS).where(USER_ID, user.getId());
        jdbcTemplate.update(helper.toString());
        return user;
    }

    @Override
    public void addFriend(User user, User friend) {
        log.debug("Добавление дружбы userId={}, friendId={}", user.getId(), friend.getId());
        String sqlGetFriendship = "SELECT COUNT(*) AS count FROM friendship WHERE friendship_user_id = ? AND friendship_friend_id = ?";
        Integer userFriendListSize = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sqlGetFriendship,
                (rs, rowNum) -> rs.getInt("count"),
                user.getId(), friend.getId()), 0);
        if (userFriendListSize == 0) {
            Integer friendFriendListSize = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sqlGetFriendship,
                    (rs, rowNum) -> rs.getInt("count"), friend.getId(), user.getId()), 0);
            String status = FriendshipStatus.UNACCEPTED.toString();
            if (friendFriendListSize > 0) {
                status = FriendshipStatus.ACCEPTED.toString();
                jdbcTemplate.update("UPDATE friendship SET friendship_status = ? WHERE friendship_user_id = ?",
                        status, friend.getId());
            }
            jdbcTemplate.update("INSERT INTO friendship (friendship_user_id, friendship_friend_id, friendship_status) VALUES (?, ?, ?)",
                    user.getId(), friend.getId(), status);
        }
    }

    @Override
    public void removeFriend(User user, User friend) {
        log.debug("Удаление дружбы userId={}, friendId={}", user.getId(), friend.getId());
        int updated = jdbcTemplate.update("DELETE FROM friendship WHERE friendship_user_id = ? AND friendship_friend_id = ?",
                user.getId(), friend.getId());
        if (updated > 0) {
            jdbcTemplate.update("UPDATE friendship SET friendship_status = ? WHERE friendship_user_id = ? AND friendship_friend_id = ?",
                    FriendshipStatus.UNACCEPTED.toString(), friend.getId(), user.getId());
        }
    }

    @Override
    public Collection<User> getFriends(User user) {
        log.debug("Получение списка друзей пользователя id={}", user.getId());
        return jdbcTemplate.query("SELECT u.* FROM friendship AS fr " +
                        "LEFT JOIN users AS u ON fr.friendship_friend_id = u.user_id WHERE fr.friendship_user_id = ?",
                (rs, rowNum) -> UserMapper.createUser(rs), user.getId());
    }
}
