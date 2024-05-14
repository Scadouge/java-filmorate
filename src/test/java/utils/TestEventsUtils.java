package utils;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.List;

public class TestEventsUtils {
    EventService eventService;

    public List<Event> findEventsByUserId(Long idUser) {
        return eventService.findEventsByUserId(idUser);
    }

    public List<Event> findEventsByUserNull() {
        return findEventsByUserId(null);
    }

    public List<Event> findEventsByUserIdTooMuch() {
        return findEventsByUserId(-9999L);
    }
}
