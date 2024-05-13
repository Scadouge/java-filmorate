package ru.yandex.practicum.filmorate.dao.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.DIRECTOR_ID;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.DIRECTOR_NAME;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.DIRECTOR;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director put(Director director) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName(DIRECTOR.name());
        jdbcInsert.usingGeneratedKeyColumns(DIRECTOR_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(DIRECTOR_NAME.name(), director.getName());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Director updatedDirector = director.toBuilder().id(id).build();
        log.info("Добавление режиссера director={}", updatedDirector);
        return updatedDirector;
    }

    @Override
    public Director get(Long id) {
        log.debug("Получение режиссера id={}", id);
        try {
            SqlHelper helper = new SqlHelper();
            helper.select(DIRECTOR_ID, DIRECTOR_NAME).from(DIRECTOR).where(DIRECTOR_ID, id);
            return jdbcTemplate.queryForObject(helper.toString(), (rs, rowNum) -> DirectorMapper.createDirector(rs));
        } catch (EmptyResultDataAccessException e) {
            throw new ItemNotFoundException(id, String.format("Режиссер не найден id=%s", id));
        }
    }

    @Override
    public Collection<Director> get(Set<Long> directorIds) {
        log.debug("Получение режиссеров directorIds={}", directorIds);
        SqlHelper helper = new SqlHelper();
        helper.select(DIRECTOR_ID, DIRECTOR_NAME).from(DIRECTOR).where(DIRECTOR_ID, directorIds);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> DirectorMapper.createDirector(rs));
    }

    @Override
    public Collection<Director> getAll() {
        log.debug("Получение всех режиссеров");
        SqlHelper helper = new SqlHelper();
        helper.select(DIRECTOR_ID, DIRECTOR_NAME).from(DIRECTOR);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> DirectorMapper.createDirector(rs));
    }

    @Override
    public Director update(Director director) {
        log.debug("Обновление режиссера director={}", director);
        SqlHelper helper = new SqlHelper();
        helper.update(DIRECTOR_NAME).where(DIRECTOR_ID, director.getId());
        jdbcTemplate.update(helper.toString(), director.getName());
        return get(director.getId());
    }

    @Override
    public Director delete(Director director) {
        log.debug("Удаление режиссера id={}", director.getId());
        SqlHelper helper = new SqlHelper();
        helper.delete(DIRECTOR).where(DIRECTOR_ID, director.getId());
        jdbcTemplate.update(helper.toString());
        return director;
    }
}
