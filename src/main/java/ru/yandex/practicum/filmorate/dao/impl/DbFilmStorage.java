package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.dao.MpaStorage;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.*;
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
        log.info("Добавление фильма в базу данных film={}", film);
        String sql = "INSERT INTO films (name, description, duration, release_date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new DbStorageException("Сгенерированный ключ пользователя = null");
        }
        Long filmId = keyHolder.getKey().longValue();
        Film updatedFilm = film.toBuilder().id(filmId).build();
        setMpa(updatedFilm);
        setGenres(updatedFilm);
        return get(filmId);
    }

    @Override
    public Film get(Long id) {
        log.info("Получение фильма из базы данных id={}", id);
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> createFilm(rs), id);
            if (film == null) {
                throw new ItemNotFoundException(id);
            }
            Map<Long, Set<Genre>> genreMapping = getGenres(Set.of(id));
            if (genreMapping.containsKey(id)) {
                return film.toBuilder().genres(genreMapping.get(id).stream()
                        .sorted(Comparator.comparing(Genre::getId)) // для прохождения тестов
                        .collect(Collectors.toCollection(LinkedHashSet::new))).build();
            } else {
                return film;
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение всех фильмов из базы данных");
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        Map<Long, Set<Genre>> genres = getGenres();
        return jdbcTemplate.query(sql,
                (rs, roNum) -> createFilm(rs, genres.get(rs.getLong("film_id"))));
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма в базе данных");
        String sql = "UPDATE films SET name = ?, description = ?, duration = ?, release_date = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), film.getId());
        setMpa(film);
        setGenres(film);
        return get(film.getId());
    }

    @Override
    public Film delete(Film film) {
        log.info("Удаление фильма из базы данных id={}", film.getId());
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", film.getId());
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        log.info("Добавление лайка в базу данных filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                film.getId(), user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        log.info("Удаление лайка из базы данных filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                film.getId(), user.getId());
    }

    @Override
    public Collection<Film> getPopularByLikes(int count) {
        log.info("Получение списка популярных фильмов count={}", count);
        String sql = "SELECT F.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "RIGHT JOIN (SELECT film_id FROM likes " +
                "GROUP BY film_id ORDER BY COUNT(*) DESC LIMIT ?) AS CL ON CL.film_id= f.film_id " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        List<Film> filmsWithoutGenres = jdbcTemplate.query(sql, (rs, roNum) -> createFilm(rs), count);
        Map<Long, Set<Genre>> genres = getGenres(filmsWithoutGenres.stream().map(Film::getId).collect(Collectors.toSet()));
        return filmsWithoutGenres.stream()
                .map(film -> film.toBuilder().genres(genres.get(film.getId())).build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int getLikes(Film film) {
        log.info("Получение количества лайков у фильма id={}", film.getId());
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) AS count FROM likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("count"), film.getId());
        return Objects.requireNonNullElse(count, 0);
    }

    private void setMpa(Film film) {
        log.info("Запись MPA рейтинга фильма в базу данных id={}", film.getId());
        if (film.getMpa() != null) {
            try {
                Mpa mpa = mpaStorage.get(film.getMpa().getId());
                jdbcTemplate.update("UPDATE films SET mpa_id = ? WHERE film_id = ?",
                        mpa.getId(), film.getId());
            } catch (ItemNotFoundException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }

    private void setGenres(Film film) {
        log.info("Запись жанров фильма в базу данных id={}", film.getId());
        Set<Long> allGenresId = genreStorage.getAll().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Long> filmGenresId = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Optional<Long> missingGenreId = filmGenresId.stream()
                .filter(id -> !allGenresId.contains(id)).findFirst();
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

    private Map<Long, Set<Genre>> getGenres(Set<Long> filmIds) {
        return getGenres(filmIds, false);
    }

    private Map<Long, Set<Genre>> getGenres() {
        return getGenres(Set.of(), true);
    }

    private Map<Long, Set<Genre>> getGenres(Set<Long> filmIds, boolean selectAll) {
        Map<Long, Genre> genres = new HashMap<>();
        Map<Long, Set<Genre>> genreMapping = new HashMap<>();
        if (filmIds.isEmpty() && !selectAll) {
            return genreMapping;
        }
        String sql = "SELECT fg.*, g.name AS genre_name FROM film_genre AS fg " +
                "LEFT JOIN genre AS g ON g.genre_id = fg.genre_id " +
                "WHERE " + selectAll + " OR fg.film_id IN (" +
                String.join(",", filmIds.stream().map(String::valueOf).collect(Collectors.toSet())) + ")";
        jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Long filmId = rs.getLong("film_id");
                    Long genreId = rs.getLong("genre_id");
                    String genreName = rs.getString("genre_name");
                    if (!genres.containsKey(genreId)) {
                        genres.put(genreId, Genre.builder().id(genreId).name(genreName).build());
                    }
                    if (genreMapping.containsKey(filmId)) {
                        genreMapping.get(filmId).add(genres.get(genreId));
                    } else {
                        genreMapping.put(filmId, Stream.of(genres.get(genreId))
                                .collect(Collectors.toCollection(HashSet::new)));
                    }
                    return null;
                });
        return genreMapping;
    }

    private Film createFilm(ResultSet rs) throws SQLException {
        return createFilm(rs, null);
    }

    private Film createFilm(ResultSet rs, Set<Genre> genres) throws SQLException {
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
