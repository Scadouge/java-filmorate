package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.dao.event.DbEventStorage;
import ru.yandex.practicum.filmorate.model.Event;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbEventStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private DbEventStorage eventStorage;

    @BeforeEach
    void setUp() {
        eventStorage = new DbEventStorage(jdbcTemplate);
    }

    @Test
    void saveOne() {
        Event event = new Event();
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("event_id");

        Long eventId = simpleJdbcInsert.executeAndReturnKey(event.toMap()).longValue();
    }

    @Test
    void findAllById() {
    }
}