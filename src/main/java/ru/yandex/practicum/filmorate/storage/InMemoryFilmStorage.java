package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage extends AbstractStorage<Film> implements FilmStorage {

    @Override
    public Film put(Film item) {
        Long id = item.getId();
        if (id == null) {
            id = generateId();
            item = item.toBuilder().id(id).build();
        }
        return super.put(item);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        return storage.values().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
