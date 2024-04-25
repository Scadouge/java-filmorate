package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mpa {
    @EqualsAndHashCode.Include
    Long id;
    @NotNull(message = "Название не может отсутствовать")
    String name;
    @NotNull(message = "Описание не может отсутствовать")
    String description;
}
