package com.inpost.smartpicker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DeliveryDateValidator implements ConstraintValidator<ValidDeliveryDate, LocalDate> {

    private static final int MAX_DAYS_AHEAD = 14;

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusDays(MAX_DAYS_AHEAD);

        return !value.isBefore(today) && !value.isAfter(maxAllowedDate);
    }
}