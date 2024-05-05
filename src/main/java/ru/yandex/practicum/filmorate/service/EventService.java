package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.event.EventStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public List<Event> findEventsByUserId(Long idUser) {
        userStorage.get(idUser);
        log.info("поиск всех событый для пользователя с id={}", idUser);
        return eventStorage.findAllById(idUser);
    }

    public void createAddLikeEvent(Long idUser, Long idFilm) {

        Event event = createEvent(idUser, idFilm,
                EventType.LIKE, EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} поставил лайк фильму с fid={}", idUser, idFilm);
    }

    public void createRemoveLikeEvent(Long idUser, Long idFilm) {
        Event event = createEvent(idUser, idFilm,
                EventType.LIKE, EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} убрал лайк с фильма с fid={}", idUser, idFilm);
    }

    public void createAddReviewEvent(Long idUser, Long idFilm) {
        userStorage.get(idUser);
        filmStorage.get(idFilm);
        Event event = createEvent(idUser, idFilm, EventType.REVIEW,
                EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} создал ревью с fid={}", idUser, event.getEventId());

    }

    public void createRemoveReviewEvent(Long idUser, Long reviewId) {
        userStorage.get(idUser);
        Event event = createEvent(idUser, reviewId, EventType.REVIEW,
                EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} удалил ревью с fid={}", idUser, reviewId);
    }

    public void createUpdateReviewEvent(Long idUser, Long reviewId) {
        userStorage.get(idUser);
        Event event = createEvent(idUser, reviewId, EventType.REVIEW,
                EventOperation.UPDATE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} изменил ревью с fid={}", idUser, reviewId);
    }

    public void createAddFriend(Long idUser, Long idFriend) {
        Event event = createEvent(idUser, idFriend,
                EventType.FRIEND, EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} добавил в друзья пользователя с fid={}", idUser, idFriend);
    }

    public void createRemoveFriend(Long idUser, Long idFriend) {
        Event event = createEvent(idUser, idFriend,
                EventType.FRIEND, EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} удалил из друзей пользователя с fid={}", idUser, idFriend);
    }

    private Event createEvent(Long idUser, Long idEntity, EventType eventType, EventOperation eventOperation) {
        log.info("создаем для пользователя с id={} событие с fid={}", idUser, idEntity);
        return new
                Event.Builder()
                .userId(idUser)
                .entityId(idEntity)
                .eventType(eventType)
                .eventOperation(eventOperation)
                .build();
    }
}