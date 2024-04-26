package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Slf4j
@AllArgsConstructor
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAllMpa() {
        log.info("Получение списка всех Mpa");
        return mpaStorage.getAll();
    }

    public Mpa getMpa(Long id) {
        log.info("Получение Mpa id={}", id);
        return mpaStorage.get(id);
    }
}
