package ru.yandex.practicum.filmorate.dao.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.GENRE_ID;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.GENRE_NAME;

public class GenreMapper {
    private GenreMapper() {
    }

    public static Genre createGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getLong(GENRE_ID.name()))
                .name(rs.getString(GENRE_NAME.name()))
                .build();
    }
}
