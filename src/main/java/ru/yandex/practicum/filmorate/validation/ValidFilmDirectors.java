package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {FilmDirectorsValidator.class}
)

public @interface ValidFilmDirectors {

    String message() default "Directors validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


