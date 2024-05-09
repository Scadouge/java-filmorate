package ru.yandex.practicum.filmorate.dao.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.EVENTS;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DbEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Event put(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(EVENTS.name())
                .usingGeneratedKeyColumns(EVENT_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(EVENT_USER_ID.name(), event.getUserId());
        params.put(EVENT_ENTITY_ID.name(), event.getEntityId());
        params.put(EVENT_TIMESTAMP.name(), event.getTimestamp());
        params.put(EVENT_TYPE.name(), event.getEventType().toString());
        params.put(EVENT_OPERATION.name(), event.getOperation().toString());
        Long eventId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        log.debug("Создано событие c id = {} от пользователя с id = {}", eventId, event.getUserId());
        return event.toBuilder().eventId(eventId).build();
    }

    @Override
    public List<Event> findAllById(Long userId) {
        log.debug("Получен список событий пользователя с id = {}", userId);
        SqlHelper helper = new SqlHelper();
        helper.select(EVENT_ID, EVENT_USER_ID, EVENT_ENTITY_ID, EVENT_TIMESTAMP, EVENT_TYPE, EVENT_OPERATION);
        helper.from(EVENTS);
        helper.where(EVENT_USER_ID, userId);
        helper.orderBy(EVENT_TIMESTAMP);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> EventMapper.createEvent(rs));
    }
}