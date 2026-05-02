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

    public void swapCache(Map<String, List<Locker>> newGridCache) {
        this.gridCache = newGridCache;
    }

    public List<Locker> getLockersForGrids(List<String> gridKeys) {
        return gridKeys.stream()
                .map(key -> gridCache.getOrDefault(key, Collections.emptyList()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}