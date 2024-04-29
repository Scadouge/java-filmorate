package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    // TODO: ReviewService class
    private final ReviewStorage reviewStorage;
    public Review addReview(Review review) {
        log.debug("Добавление отзыва film={}", review);
        return reviewStorage.put(review);
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
        if (review.getReviewId() == null) {
            throw new ValidationException("Не указан id отзыва");
        }
        return reviewStorage.update(review);
    }

    public Review deleteReview(Review review) {
        log.debug("Удаление отзыва review={}", review);
        if (review.getReviewId() == null) {
            throw new ValidationException("Не указан id отзыва");
        }
        return reviewStorage.delete(review);
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        log.debug("Добавление лайка отзыву с id={} от юзера с id={}", reviewId, userId);
        reviewStorage.addLikeToReview(reviewId, userId);
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        log.debug("Добавление дизлайка отзыву с id={} от юзера с id={}", reviewId, userId);
        reviewStorage.addDislikeToReview(reviewId, userId);
    }

    public void deleteLikeOrDislikeFromReview(Long reviewId, Long userId) {
        log.debug("Удаление рейтинга отзыву с id={} от юзера с id={}", reviewId, userId);
        reviewStorage.deleteLikeOrDislikeFromReview(reviewId, userId);
    }

    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, filmId);
        return reviewStorage.getAllReviewsByFilmId(filmId, count);
    }
}
