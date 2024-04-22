package utils;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Random;


public class TestFilmUtils {
    public static Film getNonExistedFilm() {
        Random random = new Random();
        return Film.builder()
                .id(-9999L)
                .name(String.valueOf(random.nextInt(10000))).description("Film desc")
                .releaseDate(LocalDate.of(2014, 10, random.nextInt(30) + 1))
                .duration(random.nextInt(30) + 1)
                .build();
    }
}
