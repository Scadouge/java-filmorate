package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    public Review addReview(Review review) {
        log.debug("Добавление отзыва review={}", review);
        userStorage.get(review.getUserId());
        filmStorage.get(review.getFilmId());
        Review newReview = reviewStorage.put(review);
        eventService.createAddReviewEvent(review.getUserId(), newReview.getReviewId());
        return newReview;
    }

    public Review getReview(Long id) {
        log.debug("Получение отзыва id={}", id);
        return reviewStorage.get(id);
    }

    public Collection<Review> getAllReviews() {
        log.debug("Получение списка всех отзывов");
        return reviewStorage.getAll();
    }

    public Review updateReview(Review review) {
        log.info("Обновление отзыва review={}", review);
        reviewStorage.get(review.getReviewId());
        Long userId = review.getReviewId();
        Long reviewId = review.getReviewId();
        eventService.createUpdateReviewEvent(userId, reviewId);
        return reviewStorage.update(review);
    }

    public Review deleteReview(Long reviewId) {
        log.debug("Удаление отзыва c id={}", reviewId);
        Review review = getReview(reviewId);
        eventService.createRemoveReviewEvent(review.getUserId(), reviewId);
        return reviewStorage.delete(review);
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        log.debug("Добавление лайка отзыву с id={} от пользователя с id={}", reviewId, userId);
        reviewStorage.addLikeToReview(reviewStorage.get(reviewId), userStorage.get(userId));
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        log.debug("Добавление дизлайка отзыву с id={} от пользователя с id={}", reviewId, userId);
        reviewStorage.addDislikeToReview(reviewStorage.get(reviewId), userStorage.get(userId));
    }

    public void deleteLikeOrDislikeFromReview(Long reviewId, Long userId) {
        log.debug("Удаление рейтинга отзыву с id={} от пользователя с id={}", reviewId, userId);
        reviewStorage.deleteLikeOrDislikeFromReview(reviewStorage.get(reviewId), userStorage.get(userId));
    }

    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, filmId);
        if (filmId == null) {
            return getAllReviews();
        }
        return reviewStorage.getAllReviewsByFilmId(filmStorage.get(filmId), count);
    }
}
