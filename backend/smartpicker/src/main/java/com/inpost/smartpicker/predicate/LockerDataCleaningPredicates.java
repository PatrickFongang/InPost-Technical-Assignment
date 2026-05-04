package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.Locker;

import java.util.function.Predicate;

public class LockerDataCleaningPredicates {
    private LockerDataCleaningPredicates() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a predicate to check if a locker has valid geographic coordinates.
     *
     * @return a {@link Predicate} that evaluates to {@code true} if the locker has non-null latitude and longitude
     */
    public static Predicate<Locker> hasValidCoordinates() {
        return locker -> locker.getLocation() != null &&
                locker.getLocation().getLatitude() != null &&
                locker.getLocation().getLongitude() != null;
    }

    /**
     * Creates a predicate to check if a locker is not a test machine or intended for other uses.
     * <p>
     * It filters out lockers where the city name contains "TEST" or "DO WYKORZYSTANIA".
     * </p>
     *
     * @return a {@link Predicate} that evaluates to {@code true} if the locker is a valid production machine
     */
    public static Predicate<Locker> isNotTestMachine() {
        return locker -> {
            if (locker.getAddressDetails() == null || locker.getAddressDetails().getCity() == null) {
                return true;
            }
            String cityUpper = locker.getAddressDetails().getCity().toUpperCase();
            return !cityUpper.contains("TEST") && !cityUpper.contains("DO WYKORZYSTANIA");
        };
    }
}
