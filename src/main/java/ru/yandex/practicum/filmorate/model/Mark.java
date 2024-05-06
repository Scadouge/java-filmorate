package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mark {
    @EqualsAndHashCode.Include
    Long filmId;
    @EqualsAndHashCode.Include
    Long userId;
    Integer rating;
}
