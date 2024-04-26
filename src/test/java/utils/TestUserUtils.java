package utils;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Random;

public class TestUserUtils {
    public static User getNewUser(Long id) {
        Random random = new Random();
        return User.builder()
                .id(id)
                .email(random.nextInt(10000) + "@email.ru")
                .login("vanya" + random.nextInt(10000))
                .birthday(LocalDate.of(1990, 1, random.nextInt(30) + 1)).build();
    }

    public static User getNewUser() {
        return getNewUser(null);
    }

    public static User getNewNonExistentUser() {
        return getNewUser(-9999L);
    }
}
