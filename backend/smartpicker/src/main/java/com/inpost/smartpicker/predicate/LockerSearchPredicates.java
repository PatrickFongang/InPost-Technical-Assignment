package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.Locker;
import java.util.function.Predicate;

public class LockerSearchPredicates {
    private LockerSearchPredicates() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static Predicate<Locker> isWithinRadius(double radiusInKm) {
        return locker -> locker.getDistance() != null && locker.getDistance() <= radiusInKm;
    }

    public static Predicate<Locker> isThermoFriendly() {
        return locker -> "Indoor".equalsIgnoreCase(locker.getLocationType());
    }
}