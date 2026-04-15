package fr.insee.genesis.controller.validation.schedule;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScheduleRequestValidator.class)
public @interface ValidScheduleRequest {
    String message() default "Invalid schedule request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
