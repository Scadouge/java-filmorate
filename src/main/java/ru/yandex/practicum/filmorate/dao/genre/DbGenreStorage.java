package ru.yandex.practicum.filmorate.dao.genre;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre put(Genre genre) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("genre");
        jdbcInsert.usingGeneratedKeyColumns("genre_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", genre.getName());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Genre updatedGenre = genre.toBuilder().id(id).build();
        log.info("Добавление жанра genre={}", updatedGenre);
        return updatedGenre;
    }

    @Override
    public Genre get(Long id) {
        log.debug("Получение жанра id={}", id);
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> GenreMapper.createGenre(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, (String.format("Жанр не найден id= %s", id)));
        }
    }

    @Override
    public Collection<Genre> get(Set<Long> genreIds) {
        log.debug("Получение жанров genreIds={}", genreIds);
        String sql = String.format("SELECT * FROM genre WHERE genre_id IN (%s)",
                String.join(",", genreIds.stream().map(String::valueOf).collect(Collectors.toSet())));
        return jdbcTemplate.query(sql, (rs, rowNum) -> GenreMapper.createGenre(rs));
    }

    @Override
    public Collection<Genre> getAll() {
        log.debug("Получение всех жанров");
        String sql = "SELECT * FROM genre";
        return jdbcTemplate.query(sql, (rs, rowNum) -> GenreMapper.createGenre(rs));
    }

    @Override
    public Genre update(Genre genre) {
        log.debug("Обновление жанра genre={}", genre);
        jdbcTemplate.update("UPDATE genre SET name = ? WHERE genre_id = ?",
                genre.getName(), genre.getId());
        return get(genre.getId());
    }

    @Override
    public Genre delete(Genre genre) {
        log.debug("Удаление жанра id={}", genre.getId());
        jdbcTemplate.update("DELETE FROM genre WHERE genre_id = ?", genre.getId());
        return genre;
    }
}
