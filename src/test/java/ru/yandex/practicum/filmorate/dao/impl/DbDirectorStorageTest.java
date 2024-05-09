package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import utils.TestDirectorUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbDirectorStorageTest {
    private final DirectorStorage directorStorage;

    @Test
    void testPutDirector() {
        Director newDirector = TestDirectorUtils.getNewDirector();
        newDirector = directorStorage.put(newDirector);
        final Director savedDirector = directorStorage.get(newDirector.getId());

        assertThat(savedDirector)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newDirector);
    }

    @Test
    void testGetDirector() {
        final Director nonExistentDirector = TestDirectorUtils.getNewNonExistentDirector();

        assertThrows(ItemNotFoundException.class, () -> directorStorage.get(nonExistentDirector.getId()));

        final Director newDirector = directorStorage.put(TestDirectorUtils.getNewDirector());

        assertDoesNotThrow(() -> directorStorage.get(newDirector.getId()));

        final Set<Long> directorIds = Stream.generate(() -> directorStorage.put(TestDirectorUtils.getNewDirector()))
                .limit(3).map(Director::getId).collect(Collectors.toSet());
        final Collection<Director> directors = directorStorage.get(directorIds);
        final Optional<Director> missingDirector = directors.stream()
                .filter(director -> !directorIds.contains(director.getId())).findFirst();

        assertFalse(missingDirector.isPresent());
        assertEquals(3, directors.size());
    }

    @Test
    void testGetAllDirectors() {
        final Director newDirector = directorStorage.put(TestDirectorUtils.getNewDirector());
        Collection<Director> allDirectors = directorStorage.getAll();

        assertTrue(allDirectors.contains(newDirector));
    }

    @Test
    void testUpdateDirector() {
        final Director newDirector = directorStorage.put(TestDirectorUtils.getNewDirector());
        final Director toUpdateDirector = TestDirectorUtils.getNewDirector(newDirector.getId());
        directorStorage.update(toUpdateDirector);
        final Director savedDirector = directorStorage.get(newDirector.getId());

        assertThat(savedDirector)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(toUpdateDirector);
    }

    @Test
    void testDeleteDirector() {
        final Director newDirector = directorStorage.put(TestDirectorUtils.getNewDirector());

        assertDoesNotThrow(() -> directorStorage.delete(newDirector));
        assertThrows(ItemNotFoundException.class, () -> directorStorage.get(newDirector.getId()));
    }
}