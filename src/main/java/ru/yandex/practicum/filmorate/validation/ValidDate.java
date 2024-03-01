package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
    validatedBy = {DateValidator.class}
)

public @interface ValidDate {

    String message() default "Date validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String targetDate() default "";

    boolean isBefore() default true;
}


