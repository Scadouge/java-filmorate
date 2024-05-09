package ru.yandex.practicum.filmorate.dao.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.SqlHelper;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.dao.SqlHelper.Field.*;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.REVIEWS;
import static ru.yandex.practicum.filmorate.dao.SqlHelper.Table.REVIEW_RATED;

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
        jdbcInsert.withTableName(REVIEWS.name());
        jdbcInsert.usingGeneratedKeyColumns(REVIEW_ID.name());
        Map<String, Object> params = new HashMap<>();
        params.put(REVIEW_CONTENT.name(), review.getContent());
        params.put(REVIEW_IS_POSITIVE.name(), review.getIsPositive());
        params.put(REVIEW_USER_ID.name(), review.getUserId());
        params.put(REVIEW_FILM_ID.name(), review.getFilmId());
        jdbcInsert.usingColumns(params.keySet().toArray(new String[0]));
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        Review updatedReview = review.toBuilder().reviewId(id).build();
        log.debug("Добавление отзыва review={}", updatedReview);
        return updatedReview;
    }

    @Override
    public Review get(Long id) {
        log.debug("Получение отзыва id={}", id);
        try {
            SqlHelper helper = new SqlHelper();
            helper.select(REVIEW_ID, REVIEW_CONTENT, REVIEW_FILM_ID, REVIEW_USER_ID, REVIEW_IS_POSITIVE, REVIEW_RATING);
            helper.from(REVIEWS);
            helper.where(REVIEW_ID, id);
            helper.groupBy(REVIEW_ID);
            return jdbcTemplate.queryForObject(helper.toString(), (rs, rowNum) -> ReviewMapper.createReview(rs));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Ошибка получения отзыва с id={}, отзыв не найден.", id);
            throw new ItemNotFoundException(id);
        }
    }

    @Override
    public Collection<Review> getAll() {
        log.info("Получение всех отзывов");
        SqlHelper helper = new SqlHelper();
        helper.select(REVIEW_ID, REVIEW_CONTENT, REVIEW_FILM_ID, REVIEW_USER_ID, REVIEW_IS_POSITIVE, REVIEW_RATING);
        helper.from(REVIEWS);
        helper.orderByDesc(REVIEW_RATING);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> ReviewMapper.createReview(rs));
    }

    @Override
    public Review update(Review review) {
        log.info("Обновление отзыва review={}", review);
        SqlHelper helper = new SqlHelper();
        helper.update(REVIEW_CONTENT, REVIEW_IS_POSITIVE).where(REVIEW_ID, review.getReviewId());
        jdbcTemplate.update(helper.toString(), review.getContent(), review.getIsPositive());
        return get(review.getReviewId());
    }

    @Override
    public Review delete(Review review) {
        log.info("Удаление отзыва id={}", review.getReviewId());
        SqlHelper helper = new SqlHelper();
        helper.delete(REVIEWS).where(REVIEW_ID, review.getReviewId());
        jdbcTemplate.update(helper.toString());
        return review;
    }

    @Override
    public void addLikeToReview(Review review, User user) {
        addScoreToReview(review, user, LIKE_VALUE);
        log.debug("Отзыву с id={} добавлен лайк от пользователя с id={}", review.getReviewId(), user.getId());
    }

    @Override
    public void addDislikeToReview(Review review, User user) {
        addScoreToReview(review, user, DISLIKE_VALUE);
        log.debug("Отзыву с id={} добавлен дизлайк от пользователя с id={}", review.getReviewId(), user.getId());
    }

    private void addScoreToReview(Review review, User user, Integer score) {
        try {
            SqlHelper helperInsert = new SqlHelper();
            LinkedHashMap<SqlHelper.Field, Object> params = new LinkedHashMap<>();
            params.put(REVIEW_RATED_REVIEW_ID, review.getReviewId());
            params.put(REVIEW_RATED_USER_ID, user.getId());
            params.put(REVIEW_RATED_RATED, score);
            helperInsert.insert(params);
            jdbcTemplate.update(helperInsert.toString());

            SqlHelper helperUpdate = new SqlHelper();
            helperUpdate.update(REVIEW_RATING).withValue(String.format("%s + %s", REVIEW_RATING, score))
                    .where(REVIEW_ID, review.getReviewId());
            jdbcTemplate.update(helperUpdate.toString());
        } catch (DataIntegrityViolationException e) {
            log.warn("Ошибка при добавлении оценки отзыву reviewId={}, userId={}", review.getReviewId(), user.getId());
        }
    }

    @Override
    public void deleteAllUserScoresFromReviews(Review review, User user) {
        SqlHelper helperGetScore = new SqlHelper();
        helperGetScore.select(REVIEW_RATED_RATED).from(REVIEW_RATED).where(REVIEW_RATED_REVIEW_ID, review.getReviewId());
        Integer score = jdbcTemplate.queryForObject(helperGetScore.toString(),
                (rs, rowNum) -> rs.getInt(REVIEW_RATED_RATED.name()));
        score *= -1;
        SqlHelper helperUpdateRating = new SqlHelper();
        helperUpdateRating.update(REVIEW_RATING).withValue(String.format("%s + %s", REVIEW_RATING, score))
                .where(REVIEW_ID, review.getReviewId());
        jdbcTemplate.update(helperUpdateRating.toString());

        SqlHelper helperDelete = new SqlHelper();
        helperDelete.delete(REVIEW_RATED)
                .where(REVIEW_RATED_REVIEW_ID, review.getReviewId()).and(REVIEW_RATED_USER_ID, user.getId());
        jdbcTemplate.update(helperDelete.toString());
        log.debug("Отзыву с id={} удалён рейтинг от пользователя с id={}", review.getReviewId(), user.getId());
    }

    @Override
    public void deleteAllUserScoresFromReviews(User user) {
        Map<Integer, List<Long>> scoreReviews = new HashMap<>();
        SqlHelper helperGetReviews = new SqlHelper();
        helperGetReviews.select(REVIEW_RATED_RATED, REVIEW_RATED_REVIEW_ID).from(REVIEW_RATED)
                .where(REVIEW_RATED_USER_ID, user.getId());
        jdbcTemplate.query(helperGetReviews.toString(), (rs, rowNum) -> {
            long reviewId = rs.getLong(REVIEW_RATED_REVIEW_ID.name());
            int score = rs.getInt(REVIEW_RATED_RATED.name());
            if (!scoreReviews.containsKey(score)) {
                scoreReviews.put(score, new ArrayList<>());
            }
            scoreReviews.get(score).add(reviewId);
            log.debug("Оценка отзыва со значением {} от пользователя id={} помечена на удаление", score, reviewId);
            return null;
        });
        if (!scoreReviews.isEmpty()) {
            for (Map.Entry<Integer, List<Long>> entry : scoreReviews.entrySet()) {
                SqlHelper helperUpdateRating = new SqlHelper();
                int score = entry.getKey() * -1;
                helperUpdateRating.update(REVIEW_RATING).withValue(String.format("%s + %s", REVIEW_RATING, score))
                        .where(REVIEW_ID, entry.getValue());
                jdbcTemplate.update(helperUpdateRating.toString());
            }

            SqlHelper helperDelete = new SqlHelper();
            helperDelete.delete(REVIEW_RATED).where(REVIEW_RATED_USER_ID, user.getId());
            jdbcTemplate.update(helperDelete.toString());
            log.debug("Отзывам с id={} удалён рейтинг от пользователя с id={}",
                    scoreReviews.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()), user.getId());
        }
    }

    @Override
    public Collection<Review> getAllReviewsByFilmId(Film film, int count) {
        log.debug("Получение всех отзывов, или числа отзывов {} шт. для фильма с id={}", count, film.getId());
        SqlHelper helper = new SqlHelper();
        helper.select(REVIEW_ID, REVIEW_CONTENT, REVIEW_FILM_ID, REVIEW_USER_ID, REVIEW_IS_POSITIVE, REVIEW_RATING);
        helper.from(REVIEWS);
        helper.where(REVIEW_FILM_ID, film.getId());
        helper.orderByDesc(REVIEW_RATING);
        helper.limit(count);
        return jdbcTemplate.query(helper.toString(), (rs, rowNum) -> ReviewMapper.createReview(rs));
    }
}
