package ru.yandex.practicum.filmorate.dao.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DbEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveOne(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("event_id");

        Long eventId = simpleJdbcInsert.executeAndReturnKey(event.toMap()).longValue();
        log.info("Создано событие c ID = {} от пользователя с ID = {}",
                eventId,
                event.getUserId());
    }

    @Override
    public List<Event> findAllById(Long idUser) {
        String sqlQueryFindAllById = "select event_id, " +
                "       user_id, " +
                "       entity_id, " +
                "       event_timestamp, " +
                "       event_type, " +
                "       event_operation " +
                "from events " +
                "where user_id = ? " +
                "order by event_timestamp ";

        List<Event> events = jdbcTemplate.query(sqlQueryFindAllById, EventMapper::mapRowToEvent, idUser);
        log.info("Получен список событий пользователя с ID = {}", idUser);
        return events;
    }
}