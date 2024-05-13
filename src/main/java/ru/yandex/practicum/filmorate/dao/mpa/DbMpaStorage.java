package ru.yandex.practicum.filmorate.dao.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.MPA;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa put(Mpa mpa) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(MPA.name());
        jdbcInsert.usingGeneratedKeyColumns(MPA_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(MPA_NAME.name(), mpa.getName());
        params.put(MPA_DESCRIPTION.name(), mpa.getDescription());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Mpa updatedMpa = mpa.toBuilder().id(id).build();
        log.info("Добавление рейтинга mpa={}", updatedMpa);
        return updatedMpa;
    }

    @Override
    public Mpa get(Long id) {
        log.info("Получение рейтинга mpa id={}", id);
        try {
            SqlHelper helper = new SqlHelper();
            helper.select(MPA_ID, MPA_NAME, MPA_DESCRIPTION).from(MPA).where(MPA_ID, id);
            return jdbcTemplate.queryForObject(helper.toString(), (rs, rowNum) -> MpaMapper.createRating(rs));
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, (String.format("Рейтинг mpa не найден id= %s", id)));
        }
    }

    @Override
    public Collection<Mpa> getAll() {
        log.info("Получение всех рейтингов mpa");
        SqlHelper helper = new SqlHelper();
        helper.select(MPA_ID, MPA_NAME, MPA_DESCRIPTION).from(MPA);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> MpaMapper.createRating(rs));
    }

    @Override
    public Mpa update(Mpa mpa) {
        log.info("Обновление рейтинга mpa={}", mpa);
        SqlHelper helper = new SqlHelper();
        helper.update(MPA_NAME, MPA_DESCRIPTION).where(MPA_ID, mpa.getId());
        jdbcTemplate.update(helper.toString(), mpa.getName(), mpa.getDescription());
        return get(mpa.getId());
    }

    @Override
    public Mpa delete(Mpa mpa) {
        log.info("Удаление рейтинга id={}", mpa.getId());
        SqlHelper helper = new SqlHelper();
        helper.delete(MPA).where(MPA_ID, mpa.getId());
        jdbcTemplate.update(helper.toString());
        return mpa;
    }
}
