package com.inpost.smartpicker.util;

import java.util.ArrayList;
import java.util.List;

public class GeoGridUtil {

    private static final double LAT_RESOLUTION = 0.01;
    private static final double LON_RESOLUTION = 0.016;

    public static String getGridKey(double lat, double lon) {
        long latKey = Math.round(lat / LAT_RESOLUTION);
        long lonKey = Math.round(lon / LON_RESOLUTION);
        return latKey + ":" + lonKey;
    }

    public static List<String> getNeighboringGridKeys(double lat, double lon, int d) {
        List<String> keys = new ArrayList<>();
        long baseLat = Math.round(lat / LAT_RESOLUTION);
        long baseLon = Math.round(lon / LON_RESOLUTION);

        for (long dLat = -d; dLat <= d; dLat++) {
            for (long dLon = -d; dLon <= d; dLon++) {
                keys.add((baseLat + dLat) + ":" + (baseLon + dLon));
            }
        }
        return keys;
    }
}