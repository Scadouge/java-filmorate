package utils;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Random;

public class TestUserUtils {
    public static User getNewUser(Long id) {
        Random random = new Random();
        return new User(id,
                random.nextInt(10000) + "@email.ru",
                "vanya" + random.nextInt(10000),
                "Name " + random.nextInt(10000),
                LocalDate.of(1990, 1, random.nextInt(30) + 1));
    }

    public static User getNewUser() {
        return getNewUser(null);
    }

    public static User getNewNonExistentUser() {
        return getNewUser(-9999L);
    }
}
