package ru.yandex.practicum.filmorate.dao.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventMapper {

    public static Event createEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return new Event.Builder()
                .eventId(resultSet.getLong("event_id"))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .eventOperation(EventOperation.valueOf(resultSet.getString("event_operation")))
                .build();
    }
}