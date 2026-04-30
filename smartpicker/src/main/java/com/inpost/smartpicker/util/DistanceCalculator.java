package com.inpost.smartpicker.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {
    private static final int EARTH_RADIUS_KM = 6371;

    public double calculateDistanceInKm(double userLat, double userLon, double lockerLat, double lockerLon){
        double dLat = Math.toRadians(lockerLat - userLat);
        double dLon = Math.toRadians(lockerLon - userLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(lockerLat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
