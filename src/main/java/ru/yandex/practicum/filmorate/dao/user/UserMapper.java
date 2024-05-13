package ru.yandex.practicum.filmorate.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;

public class UserMapper {
    private UserMapper() {
    }

    public static User createUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong(USER_ID.name()))
                .name(rs.getString(USER_NAME.name()))
                .login(rs.getString(USER_LOGIN.name()))
                .email(rs.getString(USER_EMAIL.name()))
                .birthday(rs.getDate(USER_BIRTHDAY.name()).toLocalDate())
                .build();
    }
}
