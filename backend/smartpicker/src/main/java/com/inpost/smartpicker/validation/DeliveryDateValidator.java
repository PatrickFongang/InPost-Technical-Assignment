package com.inpost.smartpicker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DeliveryDateValidator implements ConstraintValidator<ValidDeliveryDate, LocalDate> {

    private static final int MAX_DAYS_AHEAD = 14;

    /**
     * Validates that the provided delivery date is within a valid range.
     * <p>
     * The date is considered valid if it is not null, not in the past,
     * and not more than 14 days in the future.
     * </p>
     *
     * @param value   the {@link LocalDate} to validate
     * @param context the constraint validator context
     * @return {@code true} if the date is null or within the valid range, {@code false} otherwise
     */
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