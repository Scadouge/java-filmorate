package ru.yandex.practicum.filmorate.dao.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa put(Mpa mpa) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("mpa");
        jdbcInsert.usingGeneratedKeyColumns("mpa_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", mpa.getName());
        params.put("description", mpa.getDescription());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Mpa updatedMpa = mpa.toBuilder().id(id).build();
        log.info("Добавление рейтинга mpa={}", updatedMpa);
        return updatedMpa;
    }

    @Override
    public Mpa get(Long id) {
        log.info("Получение рейтинга mpa id={}", id);
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM mpa WHERE mpa_id = ?",
                    (rs, rowNum) -> MpaMapper.createRating(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, "Рейтинг mpa не найден id=" + id);
        }
    }

    @Override
    public Collection<Mpa> getAll() {
        log.info("Получение всех рейтингов mpa");
        return jdbcTemplate.query("SELECT * FROM mpa", (rs, rowNum) -> MpaMapper.createRating(rs));
    }

    @Override
    public Mpa update(Mpa mpa) {
        log.info("Обновление рейтинга mpa={}", mpa);
        jdbcTemplate.update("UPDATE mpa SET name = ?, description = ? WHERE mpa_id = ?",
                mpa.getName(), mpa.getDescription(), mpa.getId());
        return get(mpa.getId());
    }

    @Override
    public Mpa delete(Mpa mpa) {
        log.info("Удаление рейтинга id={}", mpa.getId());
        jdbcTemplate.update("DELETE FROM mpa WHERE mpa_id = ?", mpa.getId());
        return mpa;
    }
}
