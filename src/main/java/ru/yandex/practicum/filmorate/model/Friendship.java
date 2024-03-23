package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Friendship {
    Long userId;
    Long friendId;
    Boolean isAccepted;
}
