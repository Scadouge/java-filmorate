package utils;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Random;

public class TestReviewUtils {
    public static Review getNewNonExistentReview() {
        Random random = new Random();
        return Review.builder()
                .reviewId(-9999L)
                .content(String.valueOf(random.nextInt(100)))
                .isPositive(random.nextBoolean())
                .userId((long) random.nextInt(20))
                .filmId((long) random.nextInt(20))
                .useful((long) random.nextInt(20))
                .build();
    }

    public static Review getNewReview() {
        Random random = new Random();
        return Review.builder()
                .content(String.valueOf(random.nextInt(10000)))
                .isPositive(random.nextBoolean())
                .userId((long) random.nextInt(20))
                .filmId((long) random.nextInt(20))
                .useful((long) random.nextInt(20))
                .build();
    }

}
