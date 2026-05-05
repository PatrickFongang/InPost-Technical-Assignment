package com.inpost.smartpicker.service;

import com.inpost.smartpicker.model.Locker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalLockerCacheTest {

    private LocalLockerCache localLockerCache;

    @BeforeEach
    void setUp() {
        localLockerCache = new LocalLockerCache();
    }

    @Test
    void shouldReturnEmptyListWhenCacheIsEmpty() {
        // given
        List<String> keys = List.of("grid1");

        // when
        List<Locker> result = localLockerCache.getLockersForGrids(keys);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnLockersForExistingGrids() {
        // given
        Locker locker1 = new Locker();
        locker1.setName("Locker1");
        Locker locker2 = new Locker();
        locker2.setName("Locker2");

        Map<String, List<Locker>> cache = new HashMap<>();
        cache.put("grid1", List.of(locker1));
        cache.put("grid2", List.of(locker2));

        localLockerCache.swapCache(cache);

        // when
        List<Locker> result = localLockerCache.getLockersForGrids(List.of("grid1", "grid2", "nonExistent"));

        // then
        assertThat(result).containsExactlyInAnyOrder(locker1, locker2);
    }

    @Test
    void shouldSwapCacheCorrectly() {
        // given
        Locker oldLocker = new Locker();
        oldLocker.setName("Old");
        localLockerCache.swapCache(Map.of("key", List.of(oldLocker)));

        Locker newLocker = new Locker();
        newLocker.setName("New");
        Map<String, List<Locker>> newCache = Map.of("key", List.of(newLocker));

        // when
        localLockerCache.swapCache(newCache);
        List<Locker> result = localLockerCache.getLockersForGrids(List.of("key"));

        // then
        assertThat(result).containsExactly(newLocker);
    }
}
