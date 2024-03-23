package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.filmorate.validation.ValidDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Value
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Film extends StorageItem {

    @NotBlank
    String name;
    @NotBlank
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов.")
    String description;
    @NotNull
    @ValidDate(targetDate = "1895-12-28", isBefore = false,
            message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    LocalDate releaseDate;
    @NotNull
    @Positive
    Integer duration;
    Set<Long> likes;
    Set<Genre> genre;
    Rating rating;

    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration, Set<Long> likes,
                Set<Genre> genre, Rating rating) {
        super();
        this.rating = rating;
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = Objects.requireNonNullElseGet(likes, HashSet::new);
        this.genre = Objects.requireNonNullElseGet(genre, HashSet::new);
    }
}
