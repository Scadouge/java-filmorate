package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;

public class FilmMapper {
    private FilmMapper() {
    }

    public static Set<Film> mapFilms(List<Film> films,
                                     Map<Long, Set<Genre>> genresMapping,
                                     Map<Long, Set<Director>> directorMapping) {
        Film defaultFilm = Film.builder().build();
        return films.stream()
                .map(film -> {
                    Set<Genre> genres = defaultFilm.getGenres();
                    Set<Director> director = defaultFilm.getDirectors();
                    if (genresMapping.containsKey(film.getId())) {
                        genres = genresMapping.get(film.getId());
                    }
                    if (directorMapping.containsKey(film.getId())) {
                        director = directorMapping.get(film.getId());
                    }
                    return film.toBuilder().genres(genres).directors(director).build();
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Film createFilm(ResultSet rs) throws SQLException {
        long mpaId = rs.getLong(FILM_MPA_ID.name());
        Mpa mpa = null;
        if (mpaId != 0) {
            mpa = Mpa.builder()
                    .id(mpaId)
                    .name(rs.getString(MPA_NAME.name()))
                    .description(rs.getString(MPA_DESCRIPTION.name())).build();
        }
        Long filmId = rs.getLong(FILM_ID.name());
        return Film.builder()
                .id(filmId)
                .name(rs.getString(FILM_NAME.name()))
                .description(rs.getString(FILM_DESCRIPTION.name()))
                .duration(rs.getInt(FILM_DURATION.name()))
                .releaseDate(rs.getDate(FILM_RELEASE_DATE.name()).toLocalDate())
                .mpa(mpa)
                .rating(rs.getInt(FILM_RATING.name()))
                .build();
    }
}
