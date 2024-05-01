package utils;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Random;
import java.util.Set;


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

    public static Film getNewFilm() {
        Random random = new Random();
        return Film.builder()
                .name(String.valueOf(random.nextInt(10000))).description("Film desc")
                .releaseDate(LocalDate.of(2014, 10, random.nextInt(30) + 1))
                .duration(random.nextInt(30) + 1)
                .build();
    }

    public static Film getNewFilmWithMpaAndGenres() {
        return getNewFilm().toBuilder()
                .genres(Set.of(Genre.builder().id(1L).name("Genre 1").build(),
                                Genre.builder().id(2L).name("Genre 2").build()))
                .mpa(Mpa.builder().id(1L).name("Mpa 1").description("Mpa desc 1").build())
                .build();
    }

    public static Film getNewFilmWithGenreAndYear(Genre genre, String year) {
        return getNewFilm().toBuilder()
                .genres(Set.of(genre))
                .releaseDate(LocalDate.parse(year + "-01-01"))
                .build();
    }
}
