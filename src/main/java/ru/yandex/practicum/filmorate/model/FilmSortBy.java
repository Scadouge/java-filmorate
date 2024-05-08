package ru.yandex.practicum.filmorate.model;

public enum FilmSortBy {
    YEAR,
    LIKES,
    UNKNOWN;

    public static FilmSortBy getSortBy(String sortBy) {
        try {
            return valueOf(sortBy.toUpperCase());
        } catch (Throwable e) {
            return UNKNOWN;
        }
    }
}
