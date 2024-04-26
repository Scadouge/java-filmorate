package ru.yandex.practicum.filmorate.dao.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Repository
@AllArgsConstructor
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User put(User user) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("users");
        jdbcInsert.usingGeneratedKeyColumns("user_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", user.getName());
        params.put("login", user.getLogin());
        params.put("email", user.getEmail());
        params.put("birthday", Date.valueOf(user.getBirthday()));
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        User updatedUser = user.toBuilder().id(id).build();
        log.info("Добавление пользователя user={}", updatedUser);
        return updatedUser;
    }

    @Override
    public User get(Long id) {
        log.info("Получение пользователя id={}", id);
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> UserMapper.createUser(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение всех пользователей");
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> UserMapper.createUser(rs));
    }

    @Override
    public User update(User user) {
        log.info("Обновление пользователя user={}", user);
        String sql = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getEmail(), user.getBirthday(), user.getId());
        return get(user.getId());
    }

    @Override
    public User delete(User user) {
        log.info("Удаление пользователя id={}", user.getId());
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", user.getId());
        return user;
    }

    @Override
    public void addFriend(User user, User friend) {
        log.info("Добавление дружбы userId={}, friendId={}", user.getId(), friend.getId());
        String sqlGetFriendship = "SELECT COUNT(*) AS count FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer userFriendListSize = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sqlGetFriendship,
                (rs, rowNum) -> rs.getInt("count"),
                user.getId(), friend.getId()), 0);
        if (userFriendListSize == 0) {
            Integer friendFriendListSize = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sqlGetFriendship,
                    (rs, rowNum) -> rs.getInt("count"),
                    friend.getId(), user.getId()), 0);
            String status = FriendshipStatus.UNACCEPTED.toString();
            if (friendFriendListSize > 0) {
                status = FriendshipStatus.ACCEPTED.toString();
                jdbcTemplate.update("UPDATE friendship SET status = ? WHERE user_id = ?",
                        status, friend.getId());
            }
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)",
                    user.getId(), friend.getId(), status);
        }
    }

    @Override
    public void removeFriend(User user, User friend) {
        log.info("Удаление дружбы userId={}, friendId={}", user.getId(), friend.getId());
        int updated = jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?",
                user.getId(), friend.getId());
        if (updated > 0) {
            jdbcTemplate.update("UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.UNACCEPTED.toString(), friend.getId(), user.getId());
        }
    }

    @Override
    public Collection<User> getFriends(User user) {
        log.info("Получение списка друзей пользователя id={}", user.getId());
        return jdbcTemplate.query("SELECT u.* FROM friendship AS fr " +
                        "LEFT JOIN users AS u ON fr.friend_id = u.user_id WHERE fr.user_id = ?",
                (rs, rowNum) -> UserMapper.createUser(rs), user.getId());
    }
}
