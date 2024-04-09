package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MpaStorage;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

@Slf4j
@Repository
@AllArgsConstructor
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa put(Mpa mpa) {
        log.info("Добавление mpa в базу данных mpa={}", mpa);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO mpa (name, description) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, mpa.getName());
            ps.setString(2, mpa.getDescription());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new DbStorageException("Сгенерированный ключ mpa = null");
        }
        Long key = (long) (int) keyHolder.getKey();
        return mpa.toBuilder().id(key).build();
    }

    @Override
    public Mpa get(Long id) {
        log.info("Получение рейтинга из базы данных id={}", id);
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM mpa WHERE mpa_id = ?",
                    (rs, rowNum) -> createRating(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, "Рейтинг не найден id=" + id);
        }
    }

    @Override
    public Collection<Mpa> getAll() {
        log.info("Получение всех рейтингов из базы данных");
        return jdbcTemplate.query("SELECT * FROM mpa", (rs, rowNum) -> createRating(rs));
    }

    @Override
    public Mpa update(Mpa mpa) {
        log.info("Обновление рейтинга в базе данных");
        jdbcTemplate.update("UPDATE mpa SET name = ?, description = ? WHERE mpa_id = ?",
                mpa.getName(), mpa.getDescription(), mpa.getId());
        return get(mpa.getId());
    }

    @Override
    public Mpa delete(Mpa mpa) {
        log.info("Удаление рейтинга из базы данных id={}", mpa.getId());
        jdbcTemplate.update("DELETE FROM mpa WHERE mpa_id = ?", mpa.getId());
        return mpa;
    }

    private Mpa createRating(ResultSet rs) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong("mpa_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .build();
    }
}
