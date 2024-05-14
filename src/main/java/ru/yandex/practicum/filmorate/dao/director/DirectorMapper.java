package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.DIRECTOR_ID;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.DIRECTOR_NAME;

public class DirectorMapper {
    private DirectorMapper() {
    }

    public static Director createDirector(ResultSet rs) throws SQLException {
        return Director.builder()
                .id(rs.getLong(DIRECTOR_ID.name()))
                .name(rs.getString(DIRECTOR_NAME.name()))
                .build();
    }
}
