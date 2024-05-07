package ru.yandex.practicum.filmorate.dao.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbReviewStorage implements ReviewStorage {
    public static final int LIKE_VALUE = 1;
    public static final int DISLIKE_VALUE = -1;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review put(Review review) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("reviews");
        jdbcInsert.usingGeneratedKeyColumns("review_id");
        Map<String, Object> params = new HashMap<>();
        params.put("content", review.getContent());
        params.put("is_positive", review.getIsPositive());
        params.put("user_id", review.getUserId());
        params.put("film_id", review.getFilmId());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Review updatedReview = review.toBuilder().reviewId(id).build();
        log.debug("Добавление отзыва review={}", updatedReview);
        return updatedReview;
    }

    @Override
    public Review get(Long id) {
        log.debug("Получение отзыва id={}", id);
        String sql = "SELECT r.review_id, r.content, r.film_id, r.user_id, r.is_positive, SUM(rr.rated) AS useful " +
                "FROM reviews AS r " +
                "LEFT JOIN review_rated AS rr ON r.review_id = rr.review_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive " +
                "ORDER BY useful DESC";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> ReviewMapper.createReview(rs), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Ошибка получения отзыва с id={}, отзыв не найден.", id);
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<Review> getAll() {
        log.info("Получение всех отзывов");
        String sql = "SELECT r.review_id, r.content, r.film_id, r.user_id, r.is_positive, " +
                "SUM(COALESCE(rr.rated, 0)) AS useful " +
                "FROM reviews AS r " +
                "LEFT JOIN review_rated AS rr ON r.review_id = rr.review_id " +
                "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive " +
                "ORDER BY useful DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ReviewMapper.createReview(rs));
    }

    @Override
    public Review update(Review review) {
        log.info("Обновление отзыва review={}", review);
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return get(review.getReviewId());
    }

    @Override
    public Review delete(Review review) {
        log.info("Удаление отзыва id={}", review.getReviewId());
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", review.getReviewId());
        return review;
    }

    @Override
    public void addLikeToReview(Review review, User user) {
        String sql = "INSERT INTO review_rated (review_id, user_id, rated) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, review.getReviewId(), user.getId(), LIKE_VALUE);
        log.debug("Отзыву с id={} добавлен лайк от пользователя с id={}", review.getReviewId(), user.getId());
    }

    @Override
    public void addDislikeToReview(Review review, User user) {
        String sql = "INSERT INTO review_rated (review_id, user_id, rated) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, review.getReviewId(), user.getId(), DISLIKE_VALUE);
        log.debug("Отзыву с id={} добавлен дизлайк от пользователя с id={}", review.getReviewId(), user.getId());
    }

    @Override
    public void deleteLikeOrDislikeFromReview(Review review, User user) {
        String sql = "DELETE FROM review_rated WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, review.getReviewId(), user.getId());
        log.debug("Отзыву с id={} удалён рейтинг от пользователя с id={}", review.getReviewId(), user.getId());
    }

    @Override
    public Collection<Review> getAllReviewsByFilmId(Film film, int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, film.getId());
        String sql = "SELECT r.review_id, r.content, r.film_id, r.user_id, r.is_positive, " +
                "SUM(COALESCE(rr.rated, 0)) AS useful " +
                "FROM reviews AS r " +
                "LEFT JOIN review_rated AS rr ON r.review_id = rr.review_id " +
                "WHERE film_id = ?" +
                "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ReviewMapper.createReview(rs), film.getId(), count);
    }
}
