package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;

public class ReviewMapper {
    private ReviewMapper() {
    }

    public static Review createReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong(REVIEW_ID.name()))
                .content(rs.getString(REVIEW_CONTENT.name()))
                .isPositive(rs.getBoolean(REVIEW_IS_POSITIVE.name()))
                .userId(rs.getLong(REVIEW_USER_ID.name()))
                .filmId(rs.getLong(REVIEW_FILM_ID.name()))
                .useful(rs.getInt(REVIEW_RATING.name()))
                .build();
    }
}
