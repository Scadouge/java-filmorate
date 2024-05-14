package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
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

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbReviewStorageTest {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;

    Film film;
    User user;
    Review firstReview;
    Review secondReview;

    @BeforeEach
    void setUp() {
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
        final Review savedReview = reviewStorage.get(firstReview.getId());

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(firstReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(TestReviewUtils.getNewNonExistentReview().getId()));
    }

    @Test
    void testGetAllReviews() {
        Collection<Review> allReviews = reviewStorage.getAll();
        assertTrue(allReviews.contains(firstReview));
        assertTrue(allReviews.contains(secondReview));
    }

    @Test
    void testUpdateReview() {
        Review toUpdateReview = secondReview.toBuilder().id(firstReview.getId()).useful(firstReview.getUseful()).build();

        reviewStorage.update(toUpdateReview);

        final Review savedReview = reviewStorage.get(firstReview.getId());

        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.update(TestReviewUtils.getNewNonExistentReview()));
    }

    @Test
    void testDeleteReview() {
        reviewStorage.get(firstReview.getId());
        reviewStorage.delete(firstReview);
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(firstReview.getId()));
        assertThrows(ItemNotFoundException.class, () -> reviewStorage.get(TestReviewUtils.getNewNonExistentReview().getId()));
    }

    @Test
    void testAddLikeToReview() {
        Integer usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addLikeToReview(firstReview, user);
        assertDoesNotThrow(() ->  reviewStorage.addLikeToReview(firstReview, user));
        assertDoesNotThrow(() ->  reviewStorage.addLikeToReview(firstReview, user));
        assertDoesNotThrow(() ->  reviewStorage.addLikeToReview(firstReview, user));
        assertDoesNotThrow(() ->  reviewStorage.addLikeToReview(firstReview, user));
        Review updatedReview = reviewStorage.get(firstReview.getId());
        assertEquals(usefulBeforeLike + LIKE_VALUE, updatedReview.getUseful());
    }

    @Test
    void testAddDislikeToReview() {
        Integer usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addDislikeToReview(firstReview, user);
        assertDoesNotThrow(() ->  reviewStorage.addDislikeToReview(firstReview, user));
        assertDoesNotThrow(() ->  reviewStorage.addDislikeToReview(firstReview, user));
        assertDoesNotThrow(() ->  reviewStorage.addDislikeToReview(firstReview, user));
        Review updatedReview = reviewStorage.get(firstReview.getId());
        assertEquals(usefulBeforeLike + DISLIKE_VALUE, updatedReview.getUseful());

        reviewStorage.deleteAllUserScoresFromReviews(firstReview, user);
        reviewStorage.addLikeToReview(firstReview, user);
        assertEquals(usefulBeforeLike + LIKE_VALUE, reviewStorage.get(firstReview.getId()).getUseful());
        User newUser = userStorage.put(TestUserUtils.getNewUser());
        reviewStorage.addLikeToReview(firstReview, newUser);
        assertEquals(usefulBeforeLike + (LIKE_VALUE * 2), reviewStorage.get(firstReview.getId()).getUseful());
        userStorage.delete(newUser);
        assertEquals(usefulBeforeLike + LIKE_VALUE, reviewStorage.get(firstReview.getId()).getUseful());
    }

    @Test
    void testDeleteLikeOrDislikeToReview() {
        Integer usefulBeforeLike = firstReview.getUseful();
        reviewStorage.addDislikeToReview(firstReview, user);
        Review updatedReview = reviewStorage.get(firstReview.getId());
        assertEquals(usefulBeforeLike + DISLIKE_VALUE, updatedReview.getUseful());

        reviewStorage.deleteAllUserScoresFromReviews(firstReview, user);
        updatedReview = reviewStorage.get(firstReview.getId());
        assertEquals(usefulBeforeLike, updatedReview.getUseful());
    }

    @Test
    void getAllReviewsByFilmId() {
        User newUser = userStorage.put(TestUserUtils.getNewUser());

        Review thirdReview = reviewStorage.put(TestReviewUtils.getNewReview().toBuilder()
                .filmId(film.getId())
                .userId(user.getId())
                .build());

        reviewStorage.addLikeToReview(firstReview, user);
        reviewStorage.addLikeToReview(firstReview, newUser);
        reviewStorage.addDislikeToReview(secondReview, user);

        final Collection<Review> expectedList = List.of(firstReview, thirdReview, secondReview);

        assertEquals(expectedList, reviewStorage.getAllReviewsByFilmId(film, 10));
    }
}