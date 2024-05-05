package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {
    @NotNull
    Long timestamp;
    @EqualsAndHashCode.Include
    Long eventId;
    @NotNull
    Long userId;
    @NotNull
    Long entityId;
    @NotNull
    EventType eventType;
    @NotNull
    EventOperation operation;
}