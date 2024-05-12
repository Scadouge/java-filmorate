package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.validation.ValidDate;
import ru.yandex.practicum.filmorate.validation.ValidFilmDirectors;
import ru.yandex.practicum.filmorate.validation.ValidFilmGenres;
import ru.yandex.practicum.filmorate.validation.ValidFilmMpa;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
@Jacksonized
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    @EqualsAndHashCode.Include
    Long id;
    @NotBlank(message = "Название не может отсутствовать")
    String name;
    @NotBlank(message = "Описание не может отсутствовать")
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов.")
    String description;
    @NotNull(message = "Дата релиза не может отсутствовать")
    @ValidDate(targetDate = "1895-12-28", isBefore = false,
            message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    LocalDate releaseDate;
    @NotNull(message = "Продолжительность не может отсутствовать")
    @Positive(message = "Продолжительность должна быть положительной")
    Integer duration;
    @ValidFilmGenres(message = "У жанра отсутствует id")
    @Builder.Default
    Set<Genre> genres = new HashSet<>();
    @ValidFilmMpa(message = "У рейтинга mpa отсутствует id")
    Mpa mpa;
    @ValidFilmDirectors(message = "У режиссера отсутствует id")
    @Builder.Default
    Set<Director> directors = new HashSet<>();
    @Builder.Default
    Double rating = 0.0D;
    @Builder.Default
    Integer ratingCount = 0;
}
