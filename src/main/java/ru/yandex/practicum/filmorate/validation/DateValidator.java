package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<ValidDate, LocalDate> {
    private LocalDate targetDate;
    private Boolean isBefore;

    @Override
    public void initialize(ValidDate constraintAnnotation) {
        targetDate = LocalDate.parse(constraintAnnotation.targetDate());
        isBefore = constraintAnnotation.isBefore();
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (targetDate == null || isBefore == null || localDate == null) {
            return false;
        }
        return (!isBefore && localDate.isAfter(targetDate)) || (isBefore && localDate.isBefore(targetDate));
    }
}