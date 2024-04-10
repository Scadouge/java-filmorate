package ru.yandex.practicum.filmorate.dao.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

@Slf4j
@Repository
@AllArgsConstructor
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre put(Genre genre) {
        log.info("Добавление жанра в базу данных mpa={}", genre);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO genre (name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, genre.getName());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new DbStorageException("Сгенерированный ключ жанра = null");
        }
        Long key = (long) (int) keyHolder.getKey();
        return genre.toBuilder().id(key).build();
    }

    @Override
    public Genre get(Long id) {
        log.info("Получение жанра из базы данных id={}", id);
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> createGenre(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, "Жанр не найден id=" + id);
        }
    }

    @Override
    public Collection<Genre> getAll() {
        log.info("Получение всех жанров из базы данных");
        String sql = "SELECT * FROM genre";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createGenre(rs));
    }

    @Override
    public Genre update(Genre genre) {
        log.info("Обновление жанра в базе данных");
        jdbcTemplate.update("UPDATE genre SET name = ? WHERE genre_id = ?",
                genre.getName(), genre.getId());
        return get(genre.getId());
    }

    @Override
    public Genre delete(Genre genre) {
        log.info("Удаление жанра из базы данных id={}", genre.getId());
        jdbcTemplate.update("DELETE FROM genre WHERE genre_id = ?", genre.getId());
        return genre;
    }

    private Genre createGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
