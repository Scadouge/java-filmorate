package ru.yandex.practicum.filmorate.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {
    private UserMapper() {
    }

    public static User createUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .name(rs.getString("name"))
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
