package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode
public abstract class StorageItem {
    public abstract StorageItemBuilder<?, ?> toBuilder();

    public Long getId() {
        return id;
    }

    @EqualsAndHashCode.Include
    protected Long id;
}
