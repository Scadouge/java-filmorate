package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {FilmGenresValidator.class}
)

public @interface ValidFilmGenres {

    String message() default "Genres validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


