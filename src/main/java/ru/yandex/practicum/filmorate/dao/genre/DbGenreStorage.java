package ru.yandex.practicum.filmorate.dao.genre;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.GENRE_ID;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.GENRE_NAME;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.GENRES;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre put(Genre genre) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(GENRES.name());
        jdbcInsert.usingGeneratedKeyColumns(GENRE_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(GENRE_NAME.name(), genre.getName());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Genre updatedGenre = genre.toBuilder().id(id).build();
        log.info("Добавление жанра genre={}", updatedGenre);
        return updatedGenre;
    }

    @Override
    public Genre get(Long id) {
        log.debug("Получение жанра id={}", id);
        SqlHelper helper = new SqlHelper();
        helper.select(GENRE_ID, GENRE_NAME).from(GENRES).where(GENRE_ID, id);
        try {
            return jdbcTemplate.queryForObject(helper.toString(), (rs, rowNum) -> GenreMapper.createGenre(rs));
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, (String.format("Жанр не найден id= %s", id)));
        }
    }

    @Override
    public Collection<Genre> get(Set<Long> genreIds) {
        log.debug("Получение жанров genreIds={}", genreIds);
        SqlHelper helper = new SqlHelper();
        helper.select(GENRE_ID, GENRE_NAME).from(GENRES).where(GENRE_ID, genreIds);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> GenreMapper.createGenre(rs));
    }

    @Override
    public Collection<Genre> getAll() {
        log.debug("Получение всех жанров");
        SqlHelper helper = new SqlHelper();
        helper.select(GENRE_ID, GENRE_NAME).from(GENRES);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> GenreMapper.createGenre(rs));
    }

    @Override
    public Genre update(Genre genre) {
        log.debug("Обновление жанра genre={}", genre);
        SqlHelper helper = new SqlHelper();
        helper.update(GENRE_NAME).where(GENRE_ID, genre.getId());
        jdbcTemplate.update(helper.toString(), genre.getName());
        return get(genre.getId());
    }

    @Override
    public Genre delete(Genre genre) {
        log.debug("Удаление жанра id={}", genre.getId());
        SqlHelper helper = new SqlHelper();
        helper.delete(GENRES).where(GENRE_ID, genre.getId());
        jdbcTemplate.update(helper.toString());
        return genre;
    }
}
