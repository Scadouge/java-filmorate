package ru.yandex.practicum.filmorate.dao.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    Event put(Event event);

    List<Event> findAllById(Long userId);
}