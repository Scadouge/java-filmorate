package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.Set;

public class FilmDirectorsValidator implements ConstraintValidator<ValidFilmDirectors, Set<Director>> {
    @Override
    public void initialize(ValidFilmDirectors constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Set<Director> set, ConstraintValidatorContext constraintValidatorContext) {
        Optional<Director> nullId = set.stream().filter(director -> director.getId() == null).findFirst();
        return nullId.isEmpty();
    }
}
