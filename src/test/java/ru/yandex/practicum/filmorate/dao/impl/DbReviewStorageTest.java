package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.director.DbDirectorStorage;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.DbGenreStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.DbMpaStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.dao.review.DbReviewStorage;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import utils.TestFilmUtils;
import utils.TestReviewUtils;
import utils.TestUserUtils;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.dao.review.DbReviewStorage.DISLIKE_VALUE;
import static ru.yandex.practicum.filmorate.dao.review.DbReviewStorage.LIKE_VALUE;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbReviewStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private GenreStorage genreStorage;
    private MpaStorage mpaStorage;

    private DirectorStorage directorStorage;
    private ReviewStorage reviewStorage;

    Film film;
    User user;

    Review firstReview;
    Review secondReview;

    @BeforeEach
    void setUp() {
        reviewStorage = new DbReviewStorage(jdbcTemplate);
        genreStorage = new DbGenreStorage(jdbcTemplate);
        mpaStorage = new DbMpaStorage(jdbcTemplate);
        userStorage = new DbUserStorage(jdbcTemplate);
        directorStorage = new DbDirectorStorage(jdbcTemplate);
        filmStorage = new DbFilmStorage(jdbcTemplate, genreStorage, mpaStorage, directorStorage);

        film = filmStorage.put(TestFilmUtils.getNewFilm());
        user = userStorage.put(TestUserUtils.getNewUser());

        firstReview = reviewStorage.put(TestReviewUtils
                .getNewReview()
                .toBuilder()
                .userId(user.getId())
                .filmId(film.getId())
                .build());

        secondReview = reviewStorage.put(TestReviewUtils
                .getNewReview()
                .toBuilder()
                .userId(user.getId())
                .filmId(film.getId())
                .build());

    }

    @Test
    void testPutAndGetById() {
        final Review savedReview = reviewStorage.get(firstReview.getReviewId());

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(firstReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(TestReviewUtils.getNewNonExistentReview().getReviewId()));
    }

    @Test
    void testGetAllReviews() {
        Collection<Review> allReviews = reviewStorage.getAll();
        assertTrue(allReviews.contains(firstReview));
        assertTrue(allReviews.contains(secondReview));
    }

    @Test
    void testUpdateReview() {

        Review toUpdateReview = secondReview.toBuilder().reviewId(firstReview.getReviewId()).useful(firstReview.getUseful()).build();

        reviewStorage.update(toUpdateReview);

        final Review savedReview = reviewStorage.get(firstReview.getReviewId());

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.update(TestReviewUtils.getNewNonExistentReview()));
    }

    @Test
    void testDeleteReview() {
        reviewStorage.get(firstReview.getReviewId());
        reviewStorage.delete(firstReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(firstReview.getReviewId()));
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(TestReviewUtils.getNewNonExistentReview().getReviewId()));
    }

    @Test
    void testAddLikeToReview() {
        Long usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addLikeToReview(firstReview.getReviewId(), user.getId());
        Review updatedReview = reviewStorage.get(firstReview.getReviewId());
        assertEquals(usefulBeforeLike + LIKE_VALUE, updatedReview.getUseful());
    }

    @Test
    void testAddDislikeToReview() {
        Long usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addDislikeToReview(firstReview.getReviewId(), user.getId());
        Review updatedReview = reviewStorage.get(firstReview.getReviewId());
        assertEquals(usefulBeforeLike + DISLIKE_VALUE, updatedReview.getUseful());
    }

    @Test
    void testDeleteLikeOrDislikeToReview() {
        Long usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addDislikeToReview(firstReview.getReviewId(), user.getId());
        Review updatedReview = reviewStorage.get(firstReview.getReviewId());
        assertEquals(usefulBeforeLike + DISLIKE_VALUE, updatedReview.getUseful());

        reviewStorage.deleteLikeOrDislikeFromReview(firstReview.getReviewId(), user.getId());
        updatedReview = reviewStorage.get(firstReview.getReviewId());
        assertEquals(usefulBeforeLike, updatedReview.getUseful());
    }

    @Test
    void getAllReviewsByFilmId() {
        User newUser = userStorage.put(TestUserUtils.getNewUser());

        Review thirdReview = reviewStorage.put(TestReviewUtils.getNewReview().toBuilder()
                .filmId(film.getId())
                .userId(user.getId())
                .build());

        reviewStorage.addLikeToReview(firstReview.getReviewId(), user.getId());
        reviewStorage.addLikeToReview(firstReview.getReviewId(), newUser.getId());
        reviewStorage.addDislikeToReview(secondReview.getReviewId(), user.getId());

        final Collection<Review> expectedList = List.of(firstReview, thirdReview, secondReview);

        assertEquals(expectedList, reviewStorage.getAllReviewsByFilmId(film.getId(), 10));
    }
}