package ru.yandex.practicum.filmorate.dao.director;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
public class DbDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director put(Director director) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("director");
        jdbcInsert.usingGeneratedKeyColumns("director_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", director.getName());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Director updatedDirector = director.toBuilder().id(id).build();
        log.info("Добавление режиссера director={}", updatedDirector);
        return updatedDirector;
    }

    @Override
    public Director get(Long id) {
        log.debug("Получение режиссера id={}", id);
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM director WHERE director_id = ?",
                    (rs, rowNum) -> DirectorMapper.createDirector(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, "Режиссер не найден id=" + id);
        }
    }

    @Override
    public Collection<Director> get(Set<Long> directorIds) {
        log.debug("Получение режиссеров directorIds={}", directorIds);
        String sql = String.format("SELECT * FROM director WHERE director_id IN (%s)",
                String.join(",", directorIds.stream().map(String::valueOf).collect(Collectors.toSet())));
        return jdbcTemplate.query(sql, (rs, rowNum) -> DirectorMapper.createDirector(rs));
    }

    @Override
    public Collection<Director> getAll() {
        log.debug("Получение всех режиссеров");
        return jdbcTemplate.query("SELECT * FROM director", (rs, rowNum) -> DirectorMapper.createDirector(rs));
    }

    @Override
    public Director update(Director director) {
        log.debug("Обновление режиссера director={}", director);
        jdbcTemplate.update("UPDATE director SET name = ? WHERE director_id = ?",
                director.getName(), director.getId());
        return get(director.getId());
    }

    @Override
    public Director delete(Director director) {
        log.debug("Удаление режиссера id={}", director.getId());
        jdbcTemplate.update("DELETE FROM director WHERE director_id = ?", director.getId());
        return director;
    }
}
