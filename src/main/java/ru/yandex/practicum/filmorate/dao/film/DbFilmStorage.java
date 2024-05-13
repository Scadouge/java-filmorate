package ru.yandex.practicum.filmorate.dao.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.dao.director.DirectorMapper;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreMapper;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;

    @Override
    public Film put(Film film) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(FILMS.name());
        jdbcInsert.usingGeneratedKeyColumns(FILM_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(FILM_NAME.name(), film.getName());
        params.put(FILM_DESCRIPTION.name(), film.getDescription());
        params.put(FILM_DURATION.name(), film.getDuration());
        params.put(FILM_RELEASE_DATE.name(), Date.valueOf(film.getReleaseDate()));
        if (film.getMpa() != null) {
            try {
                mpaStorage.get(film.getMpa().getId());
            } catch (ItemNotFoundException e) {
                throw new ValidationException(e.getMessage());
            }
            params.put(FILM_MPA_ID.name(), film.getMpa().getId());
        }
        jdbcInsert.usingColumns(params.keySet().toArray(new String[0]));
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
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(FILMS);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        helper.where(FILM_ID, id);
        Film film;
        try {
            film = jdbcTemplate.queryForObject(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs));
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
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(FILMS);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);

        List<Film> unfinishedFilms = jdbcTemplate.query(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs));
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(), getFilmDirectorMapping());
    }

    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма film={}", film);
        Long mpaId = null;
        if (film.getMpa() != null) {
            try {
                mpaId = mpaStorage.get(film.getMpa().getId()).getId();
            } catch (ItemNotFoundException e) {
                throw new ValidationException(e.getMessage());
            }
        }
        SqlHelper helper = new SqlHelper();
        helper.update(FILM_NAME, FILM_DESCRIPTION, FILM_DURATION, FILM_RELEASE_DATE, FILM_MPA_ID);
        helper.where(FILM_ID, film.getId());
        jdbcTemplate.update(helper.toString(), film.getName(), film.getDescription(), film.getDuration(),
                film.getReleaseDate(), mpaId);
        setGenres(film);
        setDirectors(film);
        return get(film.getId());
    }

    @Override
    public Film delete(Film film) {
        log.debug("Удаление фильма id={}", film.getId());
        SqlHelper helper = new SqlHelper();
        helper.delete(FILMS);
        helper.where(FILM_ID, film.getId());
        jdbcTemplate.update(helper.toString());
        return film;
    }

    public static double calculateRating(double oldRating, int oldCount, int newCount, int userRating) {
        if (newCount == 0) {
            return 0;
        } else {
            return ((oldRating * oldCount) + userRating) / newCount;
        }
    }

    public int getUserMark(Film film, User user) {
        SqlHelper helper = new SqlHelper();
        helper.select(MARK_RATING).from(MARKS).where(MARK_FILM_ID, film.getId()).and(MARK_USER_ID, user.getId());
        try {
            Integer rating = jdbcTemplate.queryForObject(helper.toString(),
                    (rs, rowNum) -> rs.getInt(MARK_RATING.name()));
            return Objects.requireNonNullElse(rating, 0);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public boolean addMark(Film film, User user, Integer rating) {
        log.debug("Добавление оценки id={}, userId={}, rating={}", film.getId(), user.getId(), rating);
        try {
            SqlHelper helperInsert = new SqlHelper();
            LinkedHashMap<SqlHelper.Field, Object> paramsInsert = new LinkedHashMap<>();
            paramsInsert.put(MARK_FILM_ID, film.getId());
            paramsInsert.put(MARK_USER_ID, user.getId());
            paramsInsert.put(MARK_RATING, rating);
            helperInsert.insert(paramsInsert);
            int upd = jdbcTemplate.update(helperInsert.toString());
            if (upd > 0) {
                double newRating = calculateRating(film.getRating(),
                        film.getRatingCount(), film.getRatingCount() + 1, rating);
                SqlHelper helper = new SqlHelper();
                helper.update(FILM_RATING, FILM_RATING_COUNT).withValue((FILM_RATING_COUNT.name() + " + 1"));
                helper.where(FILM_ID, film.getId());
                jdbcTemplate.update(helper.toString(), newRating);
                return true;
            }
        } catch (DataIntegrityViolationException e) {
            log.warn("Ошибка при добавлении оценки filmId={}, userId={}, rating={}", film.getId(), user.getId(), rating);
        }
        return false;
    }

    @Override
    public boolean removeMark(Film film, User user) {
        log.debug("Удаление оценки filmId={}, userId={}", film.getId(), user.getId());
        int userMark = getUserMark(film, user);
        if (userMark > 0) {
            SqlHelper helperDelete = new SqlHelper();
            helperDelete.delete(MARKS);
            helperDelete.where(MARK_FILM_ID, film.getId());
            helperDelete.and(MARK_USER_ID, user.getId());
            int upd = jdbcTemplate.update(helperDelete.toString());
            if (upd > 0) {
                double newRating = calculateRating(film.getRating(),
                        film.getRatingCount(), film.getRatingCount() - 1, userMark * -1);
                SqlHelper helper = new SqlHelper();
                helper.update(FILM_RATING, FILM_RATING_COUNT).withValue(FILM_RATING_COUNT.name() + " - 1");
                helper.where(FILM_ID, film.getId());
                jdbcTemplate.update(helper.toString(), newRating);
                return true;
            }
        } else {
            log.debug("Оценка не найдена filmId={}, userId={}", film.getId(), user.getId());
        }
        return false;
    }

    @Override
    public void deleteAllUserLikesMarksFilms(User user) {
        SqlHelper helperGetLikedFilms = new SqlHelper();
        helperGetLikedFilms.select(List.of(MARKS, FILMS), MPA_NAME, MPA_DESCRIPTION).from(MARKS)
                .leftJoin(FILM_ID, MARK_FILM_ID)
                .leftJoin(MPA_ID, FILM_MPA_ID)
                .where(MARK_USER_ID, user.getId());
        Map<Film, Double> likedFilmsNewRating = new HashMap<>();
        jdbcTemplate.query(helperGetLikedFilms.toString(),
                (rs, rowNum) -> {
                    Film film = FilmMapper.createFilm(rs);
                    int rating = rs.getInt(MARK_RATING.name());
                    double newRating = calculateRating(film.getRating(),
                            film.getRatingCount(), film.getRatingCount() - 1, rating * -1);
                    likedFilmsNewRating.put(film, newRating);
                    return null;
                }
        );
        log.debug("Снижение рейтинга у фильмов filmIds={} из-за удаления пользователя userId={}",
                likedFilmsNewRating.keySet().stream().map(Film::getId).collect(Collectors.toSet()), user.getId());
        if (!likedFilmsNewRating.isEmpty()) {
            SqlHelper helperUpdate = new SqlHelper();
            helperUpdate.update(FILM_RATING, FILM_RATING_COUNT)
                    .withValue(FILM_RATING_COUNT.name() + " - 1")
                    .where(FILM_ID, "?");
            jdbcTemplate.batchUpdate(helperUpdate.toString(),
                    likedFilmsNewRating.entrySet(),
                    likedFilmsNewRating.size(),
                    (ps, entry) -> {
                        ps.setDouble(1, entry.getValue());
                        ps.setLong(2, entry.getKey().getId());
                    }
            );
            SqlHelper helperDelete = new SqlHelper();
            helperDelete.delete(MARKS);
            helperDelete.where(MARK_USER_ID, user.getId());
            jdbcTemplate.update(helperDelete.toString());
        }
    }

    @Override
    public Map<Long, HashMap<Film, Integer>> getLikedFilms() {
        log.debug("Получение списка понравившихся фильмов для каждого пользователя");
        Map<Long, HashMap<Film, Integer>> usersFilmsWithRatings = new HashMap<>();
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS, MARKS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(MARKS);
        helper.leftJoin(FILM_ID, MARK_FILM_ID);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        jdbcTemplate.query(helper.toString(), (rs, rowNum) -> {
            if (!usersFilmsWithRatings.containsKey(rs.getLong(MARK_USER_ID.name()))) {
                usersFilmsWithRatings.put(rs.getLong(MARK_USER_ID.name()), new HashMap<>());
            }
            usersFilmsWithRatings.get(rs.getLong(MARK_USER_ID.name()))
                    .put(FilmMapper.createFilm(rs), rs.getInt(MARK_RATING.name()));
            return null;
        });

        List<Film> unfinishedFilms = new ArrayList<>(); // всп. лист для сохр. незавершенных фильмов

        // заполняем всп. лист фильмами из хэшмапы
        usersFilmsWithRatings.values().forEach(map -> {
            unfinishedFilms.addAll(map.keySet());
        });

        // завершаем маппинг через всп. лист
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        Set<Film> mappedFilms = FilmMapper
                .mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));

        // заменяем фильмы в мапе на завершенные
        usersFilmsWithRatings.replaceAll((k, v) -> {
            HashMap<Film, Integer> rm = new HashMap<>();
            v.forEach((key, value) -> rm.put(mappedFilms.stream()
                    .filter(f -> f.getId().equals(key.getId()))
                    .findFirst()
                    .orElse(null), value));
            return rm;
        });

        return usersFilmsWithRatings;
    }

    @Override
    public Collection<Film> getPopularByYearAndGenre(Integer count, Long genreId, String year) {
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(FILMS);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        helper.leftJoin(FILM_GENRE_FILM_ID, FILM_ID);
        helper.leftJoin(GENRE_ID, FILM_GENRE_GENRE_ID);
        if (genreId != null && year != null) {
            helper.where(helper.equals(helper.getYear(FILM_RELEASE_DATE), year)).and(GENRE_ID, genreId);
        } else if (genreId != null) {
            helper.where(GENRE_ID, genreId);
        } else if (year != null) {
            helper.where(helper.equals(helper.getYear(FILM_RELEASE_DATE), year));
        }
        helper.groupBy(FILM_ID);
        helper.orderByDesc(FILM_RATING);
        helper.limit(count);

        List<Film> unfinishedFilms = jdbcTemplate.query(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs));
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Collection<Film> getCommonFilms(User user, User friend) {
        log.debug("Получение списка общих фильмов пользователей user={} и friend={}", user, friend);
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(MARKS);
        helper.leftJoin(FILM_ID, MARK_FILM_ID);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        helper.where(MARK_USER_ID, user.getId(), friend.getId());
        helper.groupBy(MARK_FILM_ID);
        helper.having(String.format("COUNT(%s) = %s", MARK_USER_ID.getAliasField(), 2));
        helper.orderByDesc(FILM_RATING);

        List<Film> unfinishedFilms = jdbcTemplate.query(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs));
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Collection<Film> getSortedDirectorFilms(Director director, String sortBy) {
        log.debug("Получение списка фильмов режиссера director={}, sortBy={}", director, sortBy);
        SqlHelper helper = new SqlHelper();
        FilmSortBy sortByEnum = FilmSortBy.getSortBy(sortBy);
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(FILM_DIRECTOR);
        helper.leftJoin(FILM_ID, FILM_DIRECTOR_FILM_ID);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        helper.where(FILM_DIRECTOR_DIRECTOR_ID, director.getId());
        switch (sortByEnum) {
            case YEAR:
                helper.orderBy(FILM_RELEASE_DATE);
                break;
            case LIKES:
                helper.orderByDesc(FILM_RATING);
                break;
            default:
                throw new ValidationException(String.format("Неизвестный параметр сортировки sortBy=%s", sortBy));
        }
        List<Film> unfinishedFilms = jdbcTemplate.query(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs));
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toSet());
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    @Override
    public Collection<Film> searchFilms(String query, String by) {
        log.debug("Получение списка фильмов по поисковому запросу query={}, by={}", query, by);
        String[] split = by.split(",");
        Set<String> searchBy = new HashSet<>(Arrays.asList(split));
        Set<String> filters = new HashSet<>();
        SqlHelper helper = new SqlHelper();
        helper.select(List.of(FILMS), MPA_NAME, MPA_DESCRIPTION);
        helper.from(FILMS);
        helper.leftJoin(MPA_ID, FILM_MPA_ID);
        helper.append("RIGHT JOIN(");
        if (searchBy.contains("director")) {
            searchBy.remove("director");

            SqlHelper helperDirector = new SqlHelper();
            helperDirector.select(FILM_DIRECTOR_FILM_ID);
            helperDirector.append(String.format("AS %s ", FILM_ID.name()));
            helperDirector.from(FILM_DIRECTOR);
            helperDirector.leftJoin(DIRECTOR_ID, FILM_DIRECTOR_DIRECTOR_ID);
            helperDirector.where(DIRECTOR_NAME, "ILIKE", "?");
            filters.add(helperDirector.toString());
        }
        if (searchBy.contains("title")) {
            searchBy.remove("title");

            SqlHelper helperTitle = new SqlHelper();
            helperTitle.select(FILM_ID);
            helperTitle.from(FILMS);
            helperTitle.where(FILM_NAME, "ILIKE", "?");
            filters.add(helperTitle.toString());
        }
        if (searchBy.size() > 0 || split.length == 0) {
            throw new ValidationException(String.format("Неизвестные тэги поиска: %s", searchBy));
        }
        helper.append(String.join(" UNION ", filters));
        helper.append(String.format(") AS title ON title.%s = %s ", FILM_ID, FILM_ID.getAliasField()));
        helper.orderByDesc(FILM_RATING);

        List<Film> unfinishedFilms = jdbcTemplate.query(helper.toString(), (rs, rowNum) -> FilmMapper.createFilm(rs),
                Collections.nCopies(filters.size(), "%" + query + "%").toArray());
        Set<Long> filmIds = unfinishedFilms.stream().map(Film::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        return FilmMapper.mapFilms(unfinishedFilms, getFilmGenreMapping(filmIds), getFilmDirectorMapping(filmIds));
    }

    private void setGenres(Film film) {
        log.debug("Запись жанров фильма id={}", film.getId());
        Collection<Long> existedGenreIds = genreStorage.get(film.getGenres()
                        .stream().map(Genre::getId).collect(Collectors.toSet()))
                .stream().map(Genre::getId).collect(Collectors.toSet());
        Optional<Long> missingGenreId = film.getGenres().stream()
                .map(Genre::getId).filter(id -> !existedGenreIds.contains(id)).findFirst();
        if (missingGenreId.isPresent()) {
            throw new ValidationException(String.format("Несуществующий жанр id=%s", missingGenreId.get()));
        }
        SqlHelper helperDelete = new SqlHelper();
        helperDelete.delete(FILM_GENRE);
        helperDelete.where(FILM_GENRE_FILM_ID, film.getId());
        jdbcTemplate.update(helperDelete.toString());

        SqlHelper helperInsert = new SqlHelper();
        LinkedHashMap<SqlHelper.Field, Object> insertValues = new LinkedHashMap<>();
        insertValues.put(FILM_GENRE_FILM_ID, "?");
        insertValues.put(FILM_GENRE_GENRE_ID, "?");
        helperInsert.insert(insertValues);
        jdbcTemplate.batchUpdate(helperInsert.toString(),
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
            throw new ValidationException(String.format("Несуществующий режиссер id=%s", missingDirectorId.get()));
        }
        SqlHelper helperDelete = new SqlHelper();
        helperDelete.delete(FILM_DIRECTOR);
        helperDelete.where(FILM_DIRECTOR_FILM_ID, film.getId());
        jdbcTemplate.update(helperDelete.toString());

        SqlHelper helperInsert = new SqlHelper();
        LinkedHashMap<SqlHelper.Field, Object> insertValues = new LinkedHashMap<>();
        insertValues.put(FILM_DIRECTOR_FILM_ID, "?");
        insertValues.put(FILM_DIRECTOR_DIRECTOR_ID, "?");
        helperInsert.insert(insertValues);
        jdbcTemplate.batchUpdate(helperInsert.toString(),
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
        if (!selectAll && filmIds.isEmpty()) {
            return filmGenreMapping;
        }
        SqlHelper helper = new SqlHelper();
        helper.select(FILM_GENRE_FILM_ID, GENRE_ID, GENRE_NAME);
        helper.from(FILM_GENRE);
        helper.leftJoin(GENRE_ID, FILM_GENRE_GENRE_ID);
        if (!selectAll) {
            helper.where(FILM_GENRE_FILM_ID, filmIds);
        }

        jdbcTemplate.query(helper.toString(),
                (rs, rowNum) -> {
                    Long filmId = rs.getLong(FILM_GENRE_FILM_ID.name());
                    Long genreId = rs.getLong(GENRE_ID.name());
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
        if (!selectAll && filmIds.isEmpty()) {
            return filmDirectorMapping;
        }
        SqlHelper helper = new SqlHelper();
        helper.select(FILM_DIRECTOR_FILM_ID, DIRECTOR_ID, DIRECTOR_NAME);
        helper.from(FILM_DIRECTOR);
        helper.leftJoin(DIRECTOR_ID, FILM_DIRECTOR_DIRECTOR_ID);
        if (!selectAll) {
            helper.where(FILM_DIRECTOR_FILM_ID, filmIds);
        }
        jdbcTemplate.query(helper.toString(),
                (rs, rowNum) -> {
                    Long filmId = rs.getLong(FILM_DIRECTOR_FILM_ID.name());
                    Long directorId = rs.getLong(DIRECTOR_ID.name());
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
