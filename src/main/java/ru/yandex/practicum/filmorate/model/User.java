package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.filmorate.validation.ValidLogin;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class User {
    Integer id;
    @NotBlank
    @Email
    String email;
    @ValidLogin(message = "Логин не должен содержать пробелы")
    @NotBlank
    String login;
    String name;
    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
}
