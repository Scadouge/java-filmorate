package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class User {
    Integer id;
    @NotBlank
    @Email
    String email;
    @NotBlank
    String login;
    String name;
    @NotNull
    @Past(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
}
