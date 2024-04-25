package ru.yandex.practicum.filmorate.dao.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
@AllArgsConstructor
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Override
    public Film put(Film film) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("films");
        jdbcInsert.usingGeneratedKeyColumns("film_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("duration", film.getDuration());
        params.put("release_date", Date.valueOf(film.getReleaseDate()));
        if (film.getMpa() != null) {
            try {
                mpaStorage.get(film.getMpa().getId());
            } catch (ItemNotFoundException e) {
                throw new ValidationException(e.getMessage());
            }
            params.put("mpa_id", film.getMpa().getId());
        }
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Film updatedFilm = film.toBuilder().id(id).build();
        log.info("Добавление фильма film={}", updatedFilm);
        setGenres(updatedFilm);
        return get(id);
    }

    @Override
    public Film get(Long id) {
        log.info("Получение фильма id={}", id);
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> FilmMapper.createFilm(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
        if (film == null) {
            throw new ItemNotFoundException(id);
        }
        Map<Long, Set<Genre>> genreMapping = getFilmGenreMapping(Set.of(id));
        if (genreMapping.containsKey(id)) {
            return film.toBuilder().genres(genreMapping.get(id).stream()
                    .sorted(Comparator.comparing(Genre::getId)) // для прохождения тестов
                    .collect(Collectors.toCollection(LinkedHashSet::new))).build();
        } else {
            return film;
        }
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение всех фильмов");
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        Map<Long, Set<Genre>> genreMapping = getFilmGenreMapping();
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> FilmMapper.createFilm(rs, genreMapping.get(rs.getLong("film_id"))));
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма film={}", film);
        String sql = "UPDATE films SET name = ?, description = ?, duration = ?, release_date = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        Long mpaId;
        if (film.getMpa() != null) {
            try {
                mpaId = mpaStorage.get(film.getMpa().getId()).getId();
            } catch (ItemNotFoundException e) {
                throw new ValidationException(e.getMessage());
            }
        } else {
            mpaId = null;
        }
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), mpaId, film.getId());
        setGenres(film);
        return get(film.getId());
    }

    @Override
    public Film delete(Film film) {
        log.info("Удаление фильма id={}", film.getId());
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", film.getId());
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        log.info("Добавление лайка filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("MERGE INTO likes KEY(film_id, user_id) VALUES (?, ?)", film.getId(), user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        log.info("Удаление лайка filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                film.getId(), user.getId());
    }

    @Override
    public Collection<Film> getPopularByLikes(int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "RIGHT JOIN (SELECT film_id FROM likes GROUP BY film_id ORDER BY COUNT(*) DESC LIMIT ?) " +
                    "AS CL ON CL.film_id= f.film_id " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        List<Film> filmsWithoutGenres = jdbcTemplate.query(sql, (rs, rowNum) -> FilmMapper.createFilm(rs), count);
        Map<Long, Set<Genre>> genresMapping = getFilmGenreMapping(filmsWithoutGenres.stream().map(Film::getId)
                .collect(Collectors.toSet()));
        return filmsWithoutGenres.stream()
                .map(film -> film.toBuilder().genres(genresMapping.get(film.getId())).build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int getLikesCount(Film film) {
        log.info("Получение количества лайков у фильма id={}", film.getId());
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) AS count FROM likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("count"), film.getId());
        return Objects.requireNonNullElse(count, 0);
    }

    private void setGenres(Film film) {
        log.info("Запись жанров фильма id={}", film.getId());
        Set<Long> allGenresId = genreStorage.getAll().stream().map(Genre::getId).collect(Collectors.toSet());
        Optional<Long> missingGenreId = film.getGenres().stream()
                .map(Genre::getId).filter(id -> !allGenresId.contains(id)).findFirst();
        if (missingGenreId.isPresent()) {
            throw new ValidationException("Такого жанра нет id=" + missingGenreId.get());
        }
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?",
                film.getId());
        jdbcTemplate.batchUpdate("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                film.getGenres(),
                film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                }
        );
    }

    private Map<Long, Set<Genre>> getFilmGenreMapping() {
        return getFilmGenreMapping(null);
    }

    private Map<Long, Set<Genre>> getFilmGenreMapping(Set<Long> filmIds) {
        log.info("Получение маппинга жанров для фильмов filmIds={}", filmIds);
        boolean selectAll = filmIds == null;
        Map<Long, Set<Genre>> genreMapping = new HashMap<>();
        Map<Long, Genre> genresMapping = new HashMap<>();
        String filmIdsForInClause = "";
        if (!selectAll) {
            if (filmIds.isEmpty()) {
                return genreMapping;
            }
            filmIdsForInClause = String.join(",", filmIds.stream()
                    .map(String::valueOf).collect(Collectors.toSet()));
        }
        String sql = String.format("SELECT fg.*, g.name AS genre_name FROM film_genre AS fg " +
                "LEFT JOIN genre AS g ON g.genre_id = fg.genre_id " +
                "WHERE %s OR fg.film_id IN (%s)", selectAll, filmIdsForInClause);
        jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Long filmId = rs.getLong("film_id");
                    Long genreId = rs.getLong("genre_id");
                    String genreName = rs.getString("genre_name");
                    if (!genresMapping.containsKey(genreId)) {
                        genresMapping.put(genreId, Genre.builder().id(genreId).name(genreName).build());
                    }
                    if (genreMapping.containsKey(filmId)) {
                        genreMapping.get(filmId).add(genresMapping.get(genreId));
                    } else {
                        genreMapping.put(filmId, Stream.of(genresMapping.get(genreId))
                                .collect(Collectors.toCollection(HashSet::new)));
                    }
                    return null;
                });
        return genreMapping;
    }
}
