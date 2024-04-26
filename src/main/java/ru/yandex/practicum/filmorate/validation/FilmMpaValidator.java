package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.Mpa;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FilmMpaValidator implements ConstraintValidator<ValidFilmMpa, Mpa> {
    @Override
    public void initialize(ValidFilmMpa constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Mpa mpa, ConstraintValidatorContext constraintValidatorContext) {
        if (mpa != null) {
            return mpa.getId() != null;
        } else {
            return true;
        }
    }
}