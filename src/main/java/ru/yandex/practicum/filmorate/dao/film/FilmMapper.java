package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class FilmMapper {
    public static Film createFilm(ResultSet rs) throws SQLException {
        return createFilm(rs, null);
    }

    public static Film createFilm(ResultSet rs, Set<Genre> genres) throws SQLException {
        if (genres == null) {
            genres = Set.of();
        }
        long mpaId = rs.getLong("mpa_id");
        Mpa mpa = null;
        if (mpaId != 0) {
            mpa = Mpa.builder()
                    .id(mpaId)
                    .name(rs.getString("mpa_name"))
                    .description(rs.getString("mpa_description")).build();
        }
        Long filmId = rs.getLong("film_id");
        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .mpa(mpa)
                .genres(genres)
                .build();
    }
}
