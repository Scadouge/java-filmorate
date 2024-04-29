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

    @NotNull
    Boolean isPositive;

    @NotNull
    Long userId;

    @NotNull
    Long filmId;

    @Builder.Default
    Long useful = 0L;
}
