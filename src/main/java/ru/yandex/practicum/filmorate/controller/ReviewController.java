package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва {}", review);
        return reviewService.addReview(review);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable Long id) {
        log.info("Получение отзыва id={}", id);
        return reviewService.getReview(id);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва {}", review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{reviewId}")
    public Review deleteReview(@PathVariable Long reviewId) {
        log.info("Удаление отзыва id={}", reviewId);
        return reviewService.deleteReview(reviewId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка отзыву с id={} от юзера с id={}", id, userId);
        reviewService.addLikeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление дизлайка отзыву с id={} от юзера с id={}", id, userId);
        reviewService.addDislikeToReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeFromReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка у отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteLikeOrDislikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeFromReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление дизлайка у отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteLikeOrDislikeFromReview(id, userId);
    }

    @GetMapping
    public Collection<Review> getReviewsByFilmId(@RequestParam(required = false) Long filmId,
                                                 @RequestParam(defaultValue = "10") int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, filmId);
        return reviewService.getReviewsByFilmId(filmId, count);
    }
}
