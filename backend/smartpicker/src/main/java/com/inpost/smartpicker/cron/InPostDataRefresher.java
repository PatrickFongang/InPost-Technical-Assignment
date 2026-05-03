package com.inpost.smartpicker.cron;

import com.inpost.smartpicker.exception.InPostApiException;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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

        try {
            InPostResponse firstResponse = fetchInitialPage();
            if (firstResponse == null || firstResponse.getTotalPages() == null) return;

            List<Locker> allLockers = new ArrayList<>(firstResponse.getItems());
            int totalPages = firstResponse.getTotalPages();

            if (totalPages > 1) {
                allLockers.addAll(fetchAllRemainingPages(totalPages));
            }

            Set<String> lowInterestNamesSet = buildLowInterestSet(allLockers);
            Map<String, List<Locker>> groupedByGrid = processAndGroupLockers(allLockers, lowInterestNamesSet);

            localLockerCache.swapCache(groupedByGrid);
            diskSnapshotService.saveSnapshot(groupedByGrid);

            long duration = (System.currentTimeMillis() - startTime) / 1000;
            log.info("FINISHED. Cache refreshed in {} seconds. Spatial map contains {} active grids.", duration, groupedByGrid.size());

        } catch (InPostApiException e) {
            log.error("Cache refresh aborted due to InPost API failure: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected critical error during cache refresh: {}", e.getMessage(), e);
        }
    }


    private InPostResponse fetchInitialPage() {
        String url = "https://api-global-points.easypack24.net/v1/points?page=1";
        try {
            return restTemplate.getForObject(url, InPostResponse.class);
        } catch (RestClientException e) {
            throw new InPostApiException("Failed to fetch the initial page from InPost API.", e);
        }
    }

    private List<Locker> fetchAllRemainingPages(int totalPages) {
        log.info("Found {} API pages. Firing concurrent requests...", totalPages);
        List<CompletableFuture<List<Locker>>> futures = new ArrayList<>();

        for (int i = 2; i <= totalPages; i++) {
            final int page = i;
            CompletableFuture<List<Locker>> future = CompletableFuture.supplyAsync(() -> fetchPageSafely(page), ioExecutor);
            futures.add(future);
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    private List<Locker> fetchPageSafely(int page) {
        String url = String.format("https://api-global-points.easypack24.net/v1/points?page=%d", page);
        try {
            InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);
            return (response != null && response.getItems() != null) ? response.getItems() : Collections.emptyList();
        } catch (RestClientException e) {
            log.warn("Failed to fetch page {}. Skipping this page. Reason: {}", page, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Set<String> buildLowInterestSet(List<Locker> allLockers) {
        Set<String> lowInterestNamesSet = allLockers.stream()
                .filter(l -> l.getRecommendedLowInterestBoxMachinesList() != null)
                .flatMap(l -> l.getRecommendedLowInterestBoxMachinesList().stream())
                .collect(Collectors.toSet());

        log.info("Identified {} genuinely low-interest machines across the network.", lowInterestNamesSet.size());
        return lowInterestNamesSet;
    }

    private Map<String, List<Locker>> processAndGroupLockers(List<Locker> allLockers, Set<String> lowInterestNamesSet) {
        log.info("Starting strict filtering, tagging, and spatial binning for {} lockers...", allLockers.size());
        return allLockers.stream()
                .filter(hasValidCoordinates())
                .filter(isNotTestMachine())
                .map(this::normalizeCityName)
                .peek(locker -> locker.setLowInterest(lowInterestNamesSet.contains(locker.getName())))
                .collect(Collectors.groupingBy(l -> GeoGridUtil.getGridKey(
                        l.getLocation().getLatitude(),
                        l.getLocation().getLongitude()
                )));
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