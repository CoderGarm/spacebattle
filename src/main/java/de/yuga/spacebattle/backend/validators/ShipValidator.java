package de.yuga.spacebattle.backend.validators;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ShipDataValidator.class)
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface ShipValidator {

    String message() default "Ship is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
