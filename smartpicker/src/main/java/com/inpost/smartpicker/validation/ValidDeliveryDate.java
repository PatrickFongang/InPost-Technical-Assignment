package com.inpost.smartpicker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DeliveryDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDeliveryDate {

    String message() default "Delivery date must be today or up to 14 days in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}