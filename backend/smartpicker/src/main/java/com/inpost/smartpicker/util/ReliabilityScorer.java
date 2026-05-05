package com.inpost.smartpicker.util;

import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.model.enums.Reliability;

public class ReliabilityScorer {

    /**
     * Calculates the "Easy Access" reliability score for a given locker.
     * <p>
     * The score is determined based on the locker type (e.g., pok, pop),
     * whether it has an easy access zone, and whether it is marked as low interest.
     * </p>
     *
     * @param locker the {@link Locker} object to score
     * @return the calculated {@link Reliability} level
     */
    public static Reliability calculateEasyAccessScore(Locker locker) {
        boolean isPokPop = locker.getType() != null &&
                (locker.getType().contains("pok") || locker.getType().contains("pop"));
        boolean hasEasyAccessFlag = Boolean.TRUE.equals(locker.getEasyAccessZone());
        boolean isLowInterest = Boolean.TRUE.equals(locker.getLowInterest());

        if (isPokPop) {
            return Reliability.HIGH;
        }
        if (!hasEasyAccessFlag) {
            return Reliability.NONE;
        }
        if (isLowInterest) {
            return Reliability.MEDIUM;
        }

        return Reliability.LOW;
    }

    /**
     * Calculates the "Stress Free" reliability score for a given locker.
     * <p>
     * The score is determined based on whether the locker is a "super point"
     * (e.g., pok, pop, superpop) and whether it is marked as low interest.
     * </p>
     *
     * @param locker the {@link Locker} object to score
     * @return the calculated {@link Reliability} level
     */
    public static Reliability calculateStressFreeScore(Locker locker) {
        boolean isSuperPoint = locker.getType() != null &&
                (locker.getType().contains("pok") ||
                        locker.getType().contains("pop") ||
                        locker.getType().contains("parcel_locker_superpop"));
        boolean isLowInterest = Boolean.TRUE.equals(locker.getLowInterest());

        if (isSuperPoint) {
            return Reliability.HIGH;
        }
        if (isLowInterest) {
            return Reliability.MEDIUM;
        }

        return Reliability.LOW;
    }
}