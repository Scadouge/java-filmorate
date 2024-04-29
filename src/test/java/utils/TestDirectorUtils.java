package utils;

import ru.yandex.practicum.filmorate.model.Director;

public class TestDirectorUtils {
    public static Director getNewDirector(Long id) {
        return Director.builder().id(id).name("Director " + id).build();
    }

    public static Director getNewDirector() {
        return getNewDirector(null);
    }

    public static Director getNewNonExistentDirector() {
        return getNewDirector(-9999L);
    }
}
