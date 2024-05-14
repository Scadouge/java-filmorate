package ru.yandex.practicum.filmorate.dao.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;

public class MpaMapper {
    private MpaMapper() {
    }

    public static Mpa createRating(ResultSet rs) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong(MPA_ID.name()))
                .name(rs.getString(MPA_NAME.name()))
                .description(rs.getString(MPA_DESCRIPTION.name()))
                .build();
    }
}
