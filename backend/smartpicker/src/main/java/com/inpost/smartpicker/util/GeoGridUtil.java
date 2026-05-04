package com.inpost.smartpicker.util;

import java.util.ArrayList;
import java.util.List;

public class GeoGridUtil {

    private static final double LAT_RESOLUTION = 0.01;
    private static final double LON_RESOLUTION = 0.016;

    /**
     * Generates a unique grid key for a given set of latitude and longitude coordinates.
     * <p>
     * The key is calculated by rounding the coordinates based on predefined latitude and longitude resolutions.
     * </p>
     *
     * @param lat the latitude coordinate
     * @param lon the longitude coordinate
     * @return a {@link String} representing the unique grid key in the format "latKey:lonKey"
     */
    public static String getGridKey(double lat, double lon) {
        long latKey = Math.round(lat / LAT_RESOLUTION);
        long lonKey = Math.round(lon / LON_RESOLUTION);
        return latKey + ":" + lonKey;
    }

    /**
     * Retrieves a list of grid keys for the grid containing the given coordinates and its neighboring grids.
     *
     * @param lat the latitude of the center point
     * @param lon the longitude of the center point
     * @param d   the search depth (radius) in terms of grid units
     * @return a {@link List} of grid keys covering the specified area
     */
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