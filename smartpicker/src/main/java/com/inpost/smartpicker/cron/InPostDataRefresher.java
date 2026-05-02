package com.inpost.smartpicker.cron;

import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.service.DiskSnapshotService;
import com.inpost.smartpicker.service.LocalLockerCache;
import com.inpost.smartpicker.util.GeoGridUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.inpost.smartpicker.predicate.LockerDataCleaningPredicates.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class InPostDataRefresher {

    private final RestTemplate restTemplate;
    private final ExecutorService ioExecutor;
    private final LocalLockerCache localLockerCache;
    private final DiskSnapshotService diskSnapshotService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        log.info("APPLICATION READY. Initializing Locker Subsystem...");

        Map<String, List<Locker>> snapshot = diskSnapshotService.loadSnapshot();

        if (snapshot != null && !snapshot.isEmpty()) {
            localLockerCache.swapCache(snapshot);
            log.info("RAM cache successfully restored from disk! App is ready for users.");
        } else {
            log.warn("WARNING: No disk snapshot available. Users will experience empty results until the initial API fetch is complete.");
        }

        log.info("Triggering background API fetch to synchronize with InPost servers...");
        CompletableFuture.runAsync(this::executeFullRefresh, ioExecutor);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void onNightlySchedule() {
        log.info("CRON TRIGGERED: Starting nightly locker synchronization...");
        executeFullRefresh();
    }

    private void executeFullRefresh() {
        log.info("STARTING GLOBAL IN-MEMORY CACHE REFRESH...");
        long startTime = System.currentTimeMillis();

        String firstPageUrl = "https://api-global-points.easypack24.net/v1/points?page=1";
        InPostResponse firstResponse;

        try {
            firstResponse = restTemplate.getForObject(firstPageUrl, InPostResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch the initial page from InPost API: {}", e.getMessage());
            return;
        }

        if (firstResponse == null || firstResponse.getTotalPages() == null) return;

        int totalPages = firstResponse.getTotalPages();
        List<Locker> allLockers = new ArrayList<>(firstResponse.getItems());

        log.info("Found {} API pages. Firing 50 concurrent requests...", totalPages);

        List<CompletableFuture<List<Locker>>> futures = new ArrayList<>();

        for (int i = 2; i <= totalPages; i++) {
            final int page = i;
            CompletableFuture<List<Locker>> future = CompletableFuture.supplyAsync(() -> {
                String url = String.format("https://api-global-points.easypack24.net/v1/points?page=%d", page);
                try {
                    InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);
                    return (response != null && response.getItems() != null) ? response.getItems() : Collections.<Locker>emptyList();
                } catch (Exception e) {
                    log.error("Failed to fetch page {}", page);
                    return Collections.<Locker>emptyList();
                }
            }, ioExecutor);
            futures.add(future);
        }

        List<Locker> remainingLockers = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        allLockers.addAll(remainingLockers);

        log.info("Successfully fetched {} lockers. Starting strict filtering and spatial binning...", allLockers.size());

        Map<String, List<Locker>> groupedByGrid = allLockers.stream()
                .filter(hasValidCoordinates())
                .filter(isNotTestMachine())
                .map(this::normalizeCityName)
                .collect(Collectors.groupingBy(l -> GeoGridUtil.getGridKey(
                        l.getLocation().getLatitude(),
                        l.getLocation().getLongitude()
                )));

        localLockerCache.swapCache(groupedByGrid);
        diskSnapshotService.saveSnapshot(groupedByGrid);

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        log.info("FINISHED. Cache refreshed in {} seconds. Spatial map contains {} active grids.", duration, groupedByGrid.size());
    }

    private Locker normalizeCityName(Locker locker) {
        if (locker.getAddressDetails() != null && locker.getAddressDetails().getCity() != null) {
            String rawCity = locker.getAddressDetails().getCity().trim().toLowerCase();

            if (!rawCity.isEmpty()) {
                StringBuilder titleCase = new StringBuilder(rawCity.length());
                boolean nextIsCapital = true;

                for (char c : rawCity.toCharArray()) {
                    if (Character.isSpaceChar(c) || c == '-') {
                        nextIsCapital = true;
                    } else if (nextIsCapital) {
                        c = Character.toUpperCase(c);
                        nextIsCapital = false;
                    }
                    titleCase.append(c);
                }
                locker.getAddressDetails().setCity(titleCase.toString());
            }
        }
        return locker;
    }
}