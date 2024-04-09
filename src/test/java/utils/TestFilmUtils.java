package utils;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Random;


public class TestFilmUtils {
    public static Film getNonExistedFilm() {
        Random random = new Random();
        return new Film(-9999L, String.valueOf(random.nextInt(10000)), "Film desc",
                LocalDate.of(2014, 10, random.nextInt(30) + 1), random.nextInt(30) + 1,
                null, null);
    }
}
