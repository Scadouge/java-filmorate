package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage extends CrudStorage<Review> {

    void addLikeToReview(Long reviewId, Long userId);

    void addDislikeToReview(Long reviewId, Long userId);

    void deleteLikeOrDislikeFromReview(Long reviewId, Long userId);

    Collection<Review> getAllReviewsByFilmId(Long filmId, int count);

}
