package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.event.EventStorage;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final EventStorage eventStorage;
    private final DbUserStorage dbUserStorage;
    private final DbFilmStorage dbFilmStorage;

    public List<Event> findEventsByUserId(Long idUser) {
        log.info("поиск всех событый для пользователя с id={}", idUser);
        return eventStorage.findAllById(dbUserStorage.get(idUser).getId());
    }

    public void createAddLikeEvent(Long idUser, Long idFilm) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), dbFilmStorage.get(idFilm).getId(),
                EventType.LIKE, EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} поставил лайк фильму с fid={}", idUser, idFilm);
    }

    public void createRemoveLikeEvent(Long idUser, Long idFilm) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), dbFilmStorage.get(idFilm).getId(),
                EventType.LIKE, EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} убрал лайк с фильма с fid={}", idUser, idFilm);
    }

    public void createAddReviewEvent(Long idUser, Long idReview) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), idReview, EventType.REVIEW,
                EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} создал ревью с fid={}", idUser, idReview);

    }

    public void createRemoveReviewEvent(Long idUser, Long idReview) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), idReview, EventType.REVIEW,
                EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} удалил ревью с fid={}", idUser, idReview);
    }

    public void createUpdateReviewEvent(Long idUser, Long idReview) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), idReview, EventType.REVIEW,
                EventOperation.UPDATE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} изменил ревью с fid={}", idUser, idReview);
    }

    public void createAddFriend(Long idUser, Long idFriend) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), dbUserStorage.get(idFriend).getId(),
                EventType.FRIEND, EventOperation.ADD);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} добавил в друзья пользователя с fid={}", idUser, idFriend);
    }

    public void createRemoveFriend(Long idUser, Long idFriend) {
        Event event = createEvent(dbUserStorage.get(idUser).getId(), dbUserStorage.get(idFriend).getId(),
                EventType.FRIEND, EventOperation.REMOVE);
        eventStorage.saveOne(event);
        log.info("пользователь с id={} удалил из друзей пользователя с fid={}", idUser, idFriend);
    }

    private Event createEvent(Long idUser, Long idEntity, EventType eventType, EventOperation eventOperation) {
        log.info("создаем для пользователя с id={} событие с fid={}", idUser, idEntity);
        return new Event.Builder()
                .userId(idUser)
                .entityId(idEntity)
                .eventType(eventType)
                .eventOperation(eventOperation)
                .build();
    }
}