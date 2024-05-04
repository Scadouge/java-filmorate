package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.dao.CrudStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface ReviewStorage extends CrudStorage<Review> {

    void addLikeToReview(Review review, User user);

    void addDislikeToReview(Review review, User user);

    void deleteLikeOrDislikeFromReview(Review review, User user);

    Collection<Review> getAllReviewsByFilmId(Film film, int count);

}
