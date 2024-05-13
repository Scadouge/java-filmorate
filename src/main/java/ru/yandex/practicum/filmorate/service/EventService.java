package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.event.EventStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public List<Event> findEventsByUserId(Long userId) {
        log.debug("Поиск всех событый для пользователя с id={}", userId);
        userStorage.get(userId);
        return eventStorage.findAllById(userId);
    }

    public void createAddMarkEvent(Long userId, Long filmId) {
        Event event = createEvent(userId, filmId, EventType.MARK, EventOperation.ADD);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} поставил оценку фильму с id={}", userId, filmId);
    }

    public void createRemoveMarkEvent(Long userId, Long filmId) {
        Event event = createEvent(userId, filmId, EventType.MARK, EventOperation.REMOVE);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} убрал оценку с фильма с id={}", userId, filmId);
    }

    public void createAddReviewEvent(Long userId, Long filmId) {
        Event event = createEvent(userId, filmId, EventType.REVIEW, EventOperation.ADD);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} создал ревью с id={}", userId, event.getEventId());
    }

    public void createRemoveReviewEvent(Long userId, Long reviewId) {
        Event event = createEvent(userId, reviewId, EventType.REVIEW, EventOperation.REMOVE);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} удалил ревью с id={}", userId, reviewId);
    }

    public void createUpdateReviewEvent(Long userId, Long reviewId) {
        Event event = createEvent(userId, reviewId, EventType.REVIEW, EventOperation.UPDATE);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} изменил ревью с id={}", userId, reviewId);
    }

    public void createAddFriend(Long userId, Long friendId) {
        Event event = createEvent(userId, friendId, EventType.FRIEND, EventOperation.ADD);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} добавил в друзья пользователя с id={}", userId, friendId);
    }

    public void createRemoveFriend(Long userId, Long friendId) {
        Event event = createEvent(userId, friendId, EventType.FRIEND, EventOperation.REMOVE);
        eventStorage.put(event);
        log.debug("Событие: Пользователь с id={} удалил из друзей пользователя с id={}", userId, friendId);
    }

    private Event createEvent(Long userId, Long entityId, EventType eventType, EventOperation eventOperation) {
        log.debug("Создаем для пользователя с id={} событие с id={}", userId, entityId);
        return Event.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(eventOperation)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}