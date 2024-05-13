package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {
    @EqualsAndHashCode.Include
    Long reviewId;

    @NotBlank(message = "Содержание отзыва не может отсутствовать")
    String content;

    @NotNull(message = "Тип отзыва не может быть null")
    Boolean isPositive;

    @NotNull(message = "id пользователя не может быть null")
    Long userId;

    @NotNull(message = "id фильма не может быть null")
    Long filmId;

    @Builder.Default
    Integer useful = 0;
}
