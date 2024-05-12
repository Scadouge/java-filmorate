package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface ReviewStorage extends CrudStorage<Review> {

    boolean addLikeToReview(Review review, User user);

    boolean addDislikeToReview(Review review, User user);

    void deleteAllUserScoresFromReviews(Review review, User user);

    void deleteAllUserScoresFromReviews(User user);

    Collection<Review> getAllReviewsByFilmId(Film film, int count);

}
