package com.inpost.smartpicker.util;

import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.model.enums.Reliability;

public class ReliabilityScorer {

    public static Reliability calculateEasyAccessScore(Locker locker) {
        boolean isPokPop = locker.getType() != null &&
                (locker.getType().contains("pok") || locker.getType().contains("pop"));
        boolean hasEasyAccessFlag = locker.getEasyAccessZone() != null && locker.getEasyAccessZone();
        boolean isLowInterest = locker.getLowInterest();

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

    public static Reliability calculateStressFreeScore(Locker locker) {
        boolean isSuperPoint = locker.getType() != null &&
                (locker.getType().contains("pok") ||
                        locker.getType().contains("pop") ||
                        locker.getType().contains("parcel_locker_superpop"));
        boolean isLowInterest = locker.getLowInterest();

        if (isSuperPoint) {
            return Reliability.HIGH;
        }
        if (isLowInterest) {
            return Reliability.MEDIUM;
        }

        return Reliability.LOW;
    }
}