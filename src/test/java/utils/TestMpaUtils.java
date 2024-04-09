package utils;

import ru.yandex.practicum.filmorate.model.Mpa;

public class TestMpaUtils {
    public static Mpa getNewMpa(Long id) {
        return new Mpa(id, "Mpa " + id, "Mpa desc " + id);
    }

    public static Mpa getNewMpa() {
        return getNewMpa(null);
    }

    public static Mpa getNewNonExistentMpa() {
        return getNewMpa(-9999L);
    }
}
