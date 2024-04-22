package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.validation.ValidLogin;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Value
@Jacksonized
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    Long id;
    @NotBlank
    @Email(message = "Неверный формат почты")
    String email;
    @ValidLogin(message = "Логин не должен содержать пробелы")
    @NotBlank
    String login;
    @NonFinal
    String name;
    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    private User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        if (name == null || name.isBlank()) {
            this.name = login;
        } else {
            this.name = name;
        }
        this.birthday = birthday;
    }
}


