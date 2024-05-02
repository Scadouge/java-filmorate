package ru.yandex.practicum.filmorate.dao.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.director.DirectorMapper;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreMapper;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
@AllArgsConstructor
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;

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
        setDirectors(updatedFilm);
        return get(id);
    }

    @Override
    public Film get(Long id) {
        log.debug("Получение фильма id={}", id);
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
        return FilmMapper.mapFilms(List.of(film),
                        getFilmGenreMapping(Set.of(film.getId())), getFilmDirectorMapping(Set.of(film.getId())))
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Ошибка при маппинге фильма"));

    }

    @Override
    public Collection<Film> getAll() {
        log.debug("Получение всех фильмов");
        String sql = "SELECT f.*,m.name AS mpa_name,m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        List<Film> unfinishedFilms = jdbcTemplate.query(sql, (rs, rowNum) -> FilmMapper.createFilm(rs));
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(), getFilmDirectorMapping());
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма film={}", film);
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
        setDirectors(film);
        return get(film.getId());
    }

    @Override
    public Film delete(Film film) {
        log.debug("Удаление фильма id={}", film.getId());
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", film.getId());
        return film;
    }

    @Override
    public void addLike(Film film, User user) {
        log.debug("Добавление лайка filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("MERGE INTO likes KEY(film_id, user_id) VALUES (?, ?)", film.getId(), user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        log.debug("Удаление лайка filmId={}, userId={}", film.getId(), user.getId());
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                film.getId(), user.getId());
    }

    @Override
    public Collection<Film> getPopularByYearAndGenre(Integer count, Long genreId, String year) {
        String filter = "";
        List<Object> parameters;

        if (genreId != null && year != null) {
            filter = "WHERE EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? AND g.genre_id = ? ";
            parameters = List.of(year, genreId, count);
        } else if (genreId != null) {
            filter = "WHERE g.genre_id = ? ";
            parameters = List.of(genreId, count);
        } else if (year != null) {
            filter = "WHERE EXTRACT(YEAR FROM CAST(f.release_date AS date)) = ? ";
            parameters = List.of(year, count);
        } else {
            parameters = List.of(count);
        }

        String sqlSelectQuery = String.format(
                "SELECT f.*, m.name mpa_name, m.description mpa_description " +
                        "FROM public.films f " +
                        "LEFT JOIN public.mpa m ON f.mpa_id = m.mpa_id " +
                        "LEFT JOIN public.film_genre fg ON f.film_id = fg.film_id " +
                        "LEFT JOIN public.genre g ON fg.genre_id = g.genre_id " +
                        "LEFT JOIN (SELECT l.film_id, COUNT(l.user_id) likes_count " +
                        "FROM public.likes l " +
                        "GROUP BY l.film_id) t ON f.film_id = t.film_id " +
                        "%s" +
                        "ORDER BY t.likes_count DESC " +
                        "LIMIT ?", filter);

        List<Film> unfinishedFilms = jdbcTemplate.query(sqlSelectQuery, (rs, rowNum) ->
                FilmMapper.createFilm(rs), parameters.toArray());
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public int getLikesCount(Film film) {
        log.debug("Получение количества лайков у фильма id={}", film.getId());
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) AS count FROM likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("count"), film.getId());
        return Objects.requireNonNullElse(count, 0);
    }

    @Override
    public Collection<Film> getFavouriteFilms(User user) {
        log.debug("Получение списка понравившихся пользователю user={} фильмов, отсортированных по популярности", user);
        String sqlSelectQuery = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "l.user_id, t.likes_count FROM likes l " +
                "LEFT JOIN (SELECT l.film_id, COUNT(l.user_id) AS likes_count FROM likes l " +
                "GROUP BY l.film_id) AS t ON t.film_id = l.film_id " +
                "LEFT JOIN films f ON l.film_id = f.film_id " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE l.user_id = ? " +
                "ORDER BY t.likes_count DESC";
        List<Film> unfinishedFilms = jdbcTemplate.query(sqlSelectQuery, (rs, rowNum) ->
                FilmMapper.createFilm(rs), user.getId());
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Collection<Film> getSortedDirectorFilms(Director director, String sortBy) {
        log.debug("Получение списка фильмов режиссера director={}, sortBy={}", director, sortBy);
        String sql;
        switch (sortBy) {
            case "year":
                sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                        "FROM film_director AS fd " +
                        "LEFT JOIN films AS f ON f.film_id = fd.film_id " +
                        "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                        "WHERE fd.director_id = ? " +
                        "ORDER BY f.release_date";
                break;
            case "likes":
                sql = "SELECT  COUNT(l.*) AS likes, f.*, m.name AS mpa_name, m.description AS mpa_description " +
                        "FROM film_director AS fd " +
                        "LEFT JOIN films AS f ON f.film_id = fd.film_id " +
                        "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.film_id " +
                        "WHERE fd.director_id = ? " +
                        "GROUP BY f.film_id " +
                        "ORDER BY likes DESC";
                break;
            default:
                throw new ValidationException("Неизвестный параметр сортировки sortBy=" + sortBy);
        }
        List<Film> unfinishedFilms = jdbcTemplate.query(sql, (rs, rowNum) -> FilmMapper.createFilm(rs), director.getId());
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Collection<Film> searchFilms(String query, String by) {
        log.debug("Получение списка фильмов по поисковому запросу query={}, by={}", query, by);
        String[] split = by.split(",");
        Set<String> searchBy = new HashSet<>(Arrays.asList(split));
        Set<String> filters = new HashSet<>();
        if (searchBy.contains("director")) {
            searchBy.remove("director");
            filters.add("SELECT fd.film_id FROM film_director AS fd " +
                    "LEFT JOIN director AS d ON d.director_id = fd.director_id WHERE d.name ILIKE ?");
        }
        if (searchBy.contains("title")) {
            searchBy.remove("title");
            filters.add("SELECT t.film_id FROM films AS t WHERE t.name ILIKE ?");
        }
        if (searchBy.size() > 0 || split.length == 0) {
            throw new ValidationException("Неизвестные тэги поиска " + searchBy);
        }
        String sb = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "RIGHT JOIN(" +
                String.join(" UNION ", filters) +
                ") AS title ON title.film_id = f.film_id " +
                "LEFT JOIN (SELECT COUNT(*) AS count, film_id FROM likes GROUP BY film_id) AS cl ON cl.film_id= f.film_id " +
                "ORDER BY cl.count DESC";
        List<Film> unfinishedFilms = jdbcTemplate.query(sb,
                (rs, rowNum) -> FilmMapper.createFilm(rs),
                Collections.nCopies(filters.size(), "%" + query + "%").toArray());
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Map<Long, List<Film>> getLikedFilms() {
        log.debug("Получение списка понравившихся фильмов для каждого пользователя");
        Map<Long, List<Film>> usersFilms = new HashMap<>();
        jdbcTemplate.query("SELECT l.* FROM likes AS l", (rs, rowNum) -> {
            if (!usersFilms.containsKey(rs.getLong("user_id"))) {
                usersFilms.put(rs.getLong("user_id"), new ArrayList<>());
            }
            usersFilms.get(rs.getLong("user_id"))
                    .add(get(rs.getLong("film_id")));
            return null;
        });
        return usersFilms;
    }

    private void setGenres(Film film) {
        log.debug("Запись жанров фильма id={}", film.getId());
        Collection<Long> existedGenreIds = genreStorage.get(film.getGenres()
                        .stream().map(Genre::getId).collect(Collectors.toSet()))
                .stream().map(Genre::getId).collect(Collectors.toSet());
        Optional<Long> missingGenreId = film.getGenres().stream()
                .map(Genre::getId).filter(id -> !existedGenreIds.contains(id)).findFirst();
        if (missingGenreId.isPresent()) {
            throw new ValidationException("Несуществующий жанр id=" + missingGenreId.get());
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

    private void setDirectors(Film film) {
        log.debug("Запись режиссеров фильма id={}", film.getId());
        Collection<Long> existedDirectorIds = directorStorage.get(film.getDirectors()
                        .stream().map(Director::getId).collect(Collectors.toSet()))
                .stream().map(Director::getId).collect(Collectors.toSet());
        Optional<Long> missingDirectorId = film.getDirectors().stream()
                .map(Director::getId).filter(id -> !existedDirectorIds.contains(id)).findFirst();
        if (missingDirectorId.isPresent()) {
            throw new ValidationException("Несуществующий режиссер id=" + missingDirectorId.get());
        }
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?",
                film.getId());
        jdbcTemplate.batchUpdate("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)",
                film.getDirectors(),
                film.getDirectors().size(),
                (ps, director) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, director.getId());
                }
        );
    }

    private Map<Long, Set<Genre>> getFilmGenreMapping() {
        return getFilmGenreMapping(null);
    }

    private Map<Long, Set<Genre>> getFilmGenreMapping(Set<Long> filmIds) {
        log.debug("Получение маппинга жанров для фильмов filmIds={}", filmIds);
        boolean selectAll = filmIds == null;
        Map<Long, Set<Genre>> filmGenreMapping = new HashMap<>();
        Map<Long, Genre> genres = new HashMap<>();
        String filmIdsForInClause = "";
        if (!selectAll) {
            if (filmIds.isEmpty()) {
                return filmGenreMapping;
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
                    if (!genres.containsKey(genreId)) {
                        genres.put(genreId, GenreMapper.createGenre(rs));
                    }
                    if (filmGenreMapping.containsKey(filmId)) {
                        filmGenreMapping.get(filmId).add(genres.get(genreId));
                    } else {
                        filmGenreMapping.put(filmId, Stream.of(genres.get(genreId))
                                .collect(Collectors.toCollection(HashSet::new)));
                    }
                    return null;
                });
        return filmGenreMapping;
    }

    private Map<Long, Set<Director>> getFilmDirectorMapping() {
        return getFilmDirectorMapping(null);
    }

    private Map<Long, Set<Director>> getFilmDirectorMapping(Set<Long> filmIds) {
        log.debug("Получение маппинга режиссеров для фильмов filmIds={}", filmIds);
        boolean selectAll = filmIds == null;
        Map<Long, Set<Director>> filmDirectorMapping = new HashMap<>();
        Map<Long, Director> directors = new HashMap<>();
        String filmIdsForInClause = "";
        if (!selectAll) {
            if (filmIds.isEmpty()) {
                return filmDirectorMapping;
            }
            filmIdsForInClause = String.join(",", filmIds.stream()
                    .map(String::valueOf).collect(Collectors.toSet()));
        }
        String sql = String.format("SELECT fd.*, d.name AS director_name FROM film_director AS fd " +
                "LEFT JOIN director AS d ON d.director_id = fd.director_id " +
                "WHERE %s OR fd.film_id IN (%s)", selectAll, filmIdsForInClause);
        jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Long filmId = rs.getLong("film_id");
                    Long directorId = rs.getLong("director_id");
                    if (!directors.containsKey(directorId)) {
                        directors.put(directorId, DirectorMapper.createDirector(rs));
                    }
                    if (filmDirectorMapping.containsKey(filmId)) {
                        filmDirectorMapping.get(filmId).add(directors.get(directorId));
                    } else {
                        filmDirectorMapping.put(filmId, Stream.of(directors.get(directorId))
                                .collect(Collectors.toCollection(HashSet::new)));
                    }
                    return null;
                });
        return filmDirectorMapping;
    }
}
