package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
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
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film implements StorageItem {
    @EqualsAndHashCode.Include
    Long id;
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

    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration, Set<Long> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = Objects.requireNonNullElseGet(likes, HashSet::new);
    }
}
