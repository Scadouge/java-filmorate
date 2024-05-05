package ru.yandex.practicum.filmorate.dao.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DbEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Event put(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("event_id");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", event.getUserId());
        params.put("entity_id", event.getEntityId());
        params.put("event_timestamp", event.getTimestamp());
        params.put("event_type", event.getEventType().toString());
        params.put("event_operation", event.getOperation().toString());
        Long eventId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        log.debug("Создано событие c id = {} от пользователя с id = {}", eventId, event.getUserId());
        return event.toBuilder().eventId(eventId).build();
    }

    @Override
    public List<Event> findAllById(Long userId) {
        log.debug("Получен список событий пользователя с id = {}", userId);
        String sqlQueryFindAllById = "SELECT * FROM events WHERE user_id = ? ORDER BY event_timestamp";
        return jdbcTemplate.query(sqlQueryFindAllById, (rs, rowNum) -> EventMapper.createEvent(rs), userId);
    }
}