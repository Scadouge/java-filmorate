package ru.yandex.practicum.filmorate.dao.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;

public class EventMapper {
    private EventMapper() {
    }

    public static Event createEvent(ResultSet resultSet) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getLong(EVENT_ID.name()))
                .userId(resultSet.getLong(EVENT_USER_ID.name()))
                .entityId(resultSet.getLong(EVENT_ENTITY_ID.name()))
                .timestamp(resultSet.getLong(EVENT_TIMESTAMP.name()))
                .eventType(EventType.valueOf(resultSet.getString(EVENT_TYPE.name())))
                .operation(EventOperation.valueOf(resultSet.getString(EVENT_OPERATION.name())))
                .build();
    }
}