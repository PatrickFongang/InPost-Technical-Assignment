package com.inpost.smartpicker.predicate;

import com.inpost.smartpicker.model.Locker;
import java.util.function.Predicate;

public class LockerSearchPredicates {
    private LockerSearchPredicates() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    /**
     * Creates a predicate to check if a locker is within a specified radius.
     *
     * @param radiusInKm the maximum allowed distance in kilometers
     * @return a {@link Predicate} that evaluates to {@code true} if the locker's distance is within the radius
     */
    public static Predicate<Locker> isWithinRadius(double radiusInKm) {
        return locker -> locker.getDistance() != null && locker.getDistance() <= radiusInKm;
    }

    /**
     * Creates a predicate to check if a locker is "thermo-friendly" (suitable for temperature-sensitive items).
     * <p>
     * A locker is considered thermo-friendly if its location type is "Indoor".
     * </p>
     *
     * @return a {@link Predicate} that evaluates to {@code true} if the locker is indoors
     */
    public static Predicate<Locker> isThermoFriendly() {
        return locker -> "Indoor".equalsIgnoreCase(locker.getLocationType());
    }
}