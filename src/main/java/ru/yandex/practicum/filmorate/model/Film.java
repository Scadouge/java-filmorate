package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class Film {
    Integer id;
    @NotBlank
    String name;
    @NotBlank
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов.")
    String description;
    @NotNull
    LocalDate releaseDate;
    @NotNull
    @Positive
    Integer duration;
}
