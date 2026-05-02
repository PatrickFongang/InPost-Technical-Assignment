package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.Locker;

import java.util.function.Predicate;

public class LockerDataCleaningPredicates {
    private LockerDataCleaningPredicates() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Predicate<Locker> hasValidCoordinates() {
        return locker -> locker.getLocation() != null &&
                locker.getLocation().getLatitude() != null &&
                locker.getLocation().getLongitude() != null;
    }

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
