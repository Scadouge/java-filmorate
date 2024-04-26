package utils;

import ru.yandex.practicum.filmorate.model.Genre;

public class TestGenreUtils {
    public static Genre getNewGenre(Long id) {
        return Genre.builder().id(id).name("Genre " + id).build();
    }

    public static Genre getNewGenre() {
        return getNewGenre(null);
    }

    public static Genre getNewNonExistentGenre() {
        return getNewGenre(-9999L);
    }
}
