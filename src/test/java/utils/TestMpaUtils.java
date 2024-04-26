package utils;

import ru.yandex.practicum.filmorate.model.Mpa;

public class TestMpaUtils {
    public static Mpa getNewMpa(Long id) {
        return Mpa.builder().id(id).name("Mpa " + id).description("Mpa desc " + id).build();
    }

    public static Mpa getNewMpa() {
        return getNewMpa(null);
    }

    public static Mpa getNewNonExistentMpa() {
        return getNewMpa(-9999L);
    }
}
