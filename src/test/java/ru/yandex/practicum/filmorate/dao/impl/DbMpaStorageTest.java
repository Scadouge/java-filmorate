package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import utils.TestMpaUtils;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbMpaStorageTest {
    private final MpaStorage mpaStorage;

    @Test
    void testPutMpa() {
        final Mpa newMpa = mpaStorage.put(TestMpaUtils.getNewMpa());
        final Mpa savedMpa = mpaStorage.get(newMpa.getId());

        assertThat(savedMpa)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newMpa);
    }

    @Test
    void testGetMpa() {
        final Mpa nonExistentMpa = TestMpaUtils.getNewNonExistentMpa();
        assertThrows(ItemNotFoundException.class, () -> mpaStorage.get(nonExistentMpa.getId()));
        final Mpa newMpa = mpaStorage.put(TestMpaUtils.getNewMpa());
        assertDoesNotThrow(() -> mpaStorage.get(newMpa.getId()));
    }

    @Test
    void testGetAllMpa() {
        final Mpa newMpa = mpaStorage.put(TestMpaUtils.getNewMpa());
        Collection<Mpa> allMpa = mpaStorage.getAll();

        assertTrue(allMpa.contains(newMpa));
    }

    @Test
    void testUpdateMpa() {
        final Mpa newMpa = mpaStorage.put(TestMpaUtils.getNewMpa());
        final Mpa toUpdateMpa = TestMpaUtils.getNewMpa(newMpa.getId());
        mpaStorage.update(toUpdateMpa);
        final Mpa savedMpa = mpaStorage.get(newMpa.getId());

        assertThat(savedMpa)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateMpa);
    }

    @Test
    void testDeleteMpa() {
        final Mpa newMpa = mpaStorage.put(TestMpaUtils.getNewMpa());
        assertDoesNotThrow(() -> mpaStorage.delete(newMpa));
        assertThrows(ItemNotFoundException.class, () -> mpaStorage.get(newMpa.getId()));
    }
}