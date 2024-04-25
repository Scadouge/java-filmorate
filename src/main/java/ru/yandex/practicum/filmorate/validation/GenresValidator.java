package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.Genre;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.Set;

public class GenresValidator implements ConstraintValidator<ValidGenres, Set<Genre>> {
    @Override
    public void initialize(ValidGenres constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Set<Genre> set, ConstraintValidatorContext constraintValidatorContext) {
        Optional<Genre> nullId = set.stream().filter(genre -> genre.getId() == null).findFirst();
        return nullId.isEmpty();
    }
}