package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {FilmMpaValidator.class}
)

public @interface ValidFilmMpa {

    String message() default "Mpa validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


