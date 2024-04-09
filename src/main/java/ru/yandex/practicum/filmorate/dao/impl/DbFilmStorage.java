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
        Long filmId = (long) (int) keyHolder.getKey();
        Film updatedFilm = film.toBuilder().id(filmId).build();
        setMpa(updatedFilm);
        setGenres(updatedFilm);
        return get(filmId);
    }

    @Override
    public Film get(Long id) {
        log.info("Получение фильма из базы данных id={}", id);
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> createFilm(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение всех фильмов из базы данных");
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createFilm(rs));
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
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
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
        return jdbcTemplate.query("SELECT film_id, COUNT(*) AS cl FROM likes " +
                        "GROUP BY film_id ORDER BY cl DESC LIMIT ?",
                (rs, rowNum) -> get(rs.getLong("film_id")),
                count);
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
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }

    private Set<Genre> getGenres(Long filmId) {
        log.info("Получение списка жанров фильма из базы данных id={}", filmId);
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ?";
        List<Long> filmGenreIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("genre_id"), filmId);
        return genreStorage.getAll().stream()
                .filter(genre -> filmGenreIds.contains(genre.getId()))
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Film createFilm(ResultSet rs) throws SQLException {
        Long filmId = rs.getLong("film_id");
        Mpa mpa = null;
        long mpaRatingId = rs.getLong("mpa_id");
        if (mpaRatingId != 0) {
            mpa = mpaStorage.get(mpaRatingId);
        }
        Set<Genre> genres = getGenres(filmId);
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
