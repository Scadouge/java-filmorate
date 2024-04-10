package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.Collection;

@Slf4j
@Repository
@AllArgsConstructor
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User put(User user) {
        log.info("Добавление пользователя в базу данных user={}", user);
        String sql = "INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new DbStorageException("Сгенерированный ключ пользователя = null");
        }
        Long key = keyHolder.getKey().longValue();
        return user.toBuilder().id(key).build();
    }

    @Override
    public User get(Long id) {
        log.info("Получение пользователя из базы данных id={}", id);
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> createUser(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение всех пользователей из базы данных");
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createUser(rs));
    }

    @Override
    public User update(User item) {
        log.info("Обновление пользователя в базе данных");
        String sql = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, item.getName(), item.getLogin(), item.getEmail(), item.getBirthday(), item.getId());
        return get(item.getId());
    }

    @Override
    public User delete(User user) {
        log.info("Удаление пользователя из базы данных id={}", user.getId());
        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? OR friend_id = ?", user.getId(), user.getId());
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", user.getId());
        return user;
    }

    @Override
    public void addFriend(User user, User friend) {
        log.info("Добавление дружбы в базу данных userId={}, friendId={}", user.getId(), friend.getId());
        String sqlGetFriendship = "SELECT * FROM friendship WHERE user_id = ? AND friend_id = ?";
        SqlRowSet userFriendship = jdbcTemplate.queryForRowSet(sqlGetFriendship, user.getId(), friend.getId());
        SqlRowSet friendFriendship = jdbcTemplate.queryForRowSet(sqlGetFriendship, friend.getId(), user.getId());

        if (!userFriendship.next()) {
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)",
                    user.getId(), friend.getId());
        }
        if (friendFriendship.next()) {
            jdbcTemplate.update("UPDATE friendship SET status = ? WHERE user_id IN (?, ?) AND friend_id IN (?, ?)",
                    FriendshipStatus.ACCEPTED, user.getId(), friend.getId(), user.getId(), friend.getId());
        }
    }

    @Override
    public void removeFriend(User user, User friend) {
        log.info("Удаление дружбы из базы данных userId={}, friendId={}", user.getId(), friend.getId());
        int updated = jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?",
                user.getId(), friend.getId());
        if (updated > 0) {
            jdbcTemplate.update("UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?",
                    FriendshipStatus.UNACCEPTED, friend.getId(), user.getId());
        }
    }

    @Override
    public Collection<User> getFriends(User user) {
        log.info("Получение списка друзей пользователя из базы данных id={}", user.getId());
        return jdbcTemplate.query("SELECT friend_id FROM friendship WHERE user_id = ?",
                (rs, rowNum) -> get(rs.getLong("friend_id")),
                user.getId());
    }

    private User createUser(ResultSet rs) throws SQLException {
        return User.builder()
                    .id(rs.getLong("user_id"))
                    .name(rs.getString("name"))
                    .login(rs.getString("login"))
                    .email(rs.getString("email"))
                    .birthday(rs.getDate("birthday").toLocalDate())
                    .build();
    }
}
