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
@RequestMapping(path = "reviews")
@RequiredArgsConstructor
public class ReviewController {
    // TODO: ReviewController class
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

    @GetMapping
    public Collection<Review> getAllReviews() {
        log.info("Получение списка всех отзывов");
        return reviewService.getAllReviews();
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва {}", review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public Review deleteReview(@PathVariable Long id) {
        log.info("Удаление отзыва id={}", id);
        return reviewService.deleteReview(reviewService.getReview(id));
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
    public void deleteLikeFromReview(@PathVariable Long id, @PathVariable Long userId){
        log.info("Удаление лайка у отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteLikeOrDislikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeFromReview(@PathVariable Long id, @PathVariable Long userId){
        log.info("Удаление дизлайка у отзыва id={} от пользователя id={}", id, userId);
        reviewService.deleteLikeOrDislikeFromReview(id, userId);
    }

    @GetMapping("reviews")
    public Collection<Review> getReviewsByFilmId(@RequestParam(required = false) Long filmId,
                                                 @RequestParam("10") int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, filmId);
        return reviewService.getReviewsByFilmId(filmId, count);
    }
}