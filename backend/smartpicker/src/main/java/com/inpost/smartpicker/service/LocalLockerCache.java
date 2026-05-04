package com.inpost.smartpicker.service;

import com.inpost.smartpicker.model.Locker;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LocalLockerCache {

    private volatile Map<String, List<Locker>> gridCache = Collections.emptyMap();

    /**
     * Replaces the current in-memory cache with a new set of geospatial grids and lockers.
     *
     * @param newGridCache the new {@link Map} of grid identifiers to locker lists to be used
     */
    public void swapCache(Map<String, List<Locker>> newGridCache) {
        this.gridCache = newGridCache;
    }

    /**
     * Retrieves a consolidated list of lockers for the specified list of geospatial grid keys.
     *
     * @param gridKeys the list of grid keys to look up in the cache
     * @return a {@link List} of all {@link Locker} objects found in the specified grids
     */
    public List<Locker> getLockersForGrids(List<String> gridKeys) {
        return gridKeys.stream()
                .map(key -> gridCache.getOrDefault(key, Collections.emptyList()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}