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

    /**
     * Handles the application startup event to initialize the locker subsystem.
     * <p>
     * It attempts to restore the locker cache from a local disk snapshot.
     * Regardless of whether the snapshot was restored, it triggers an asynchronous
     * full refresh from the InPost API.
     * </p>
     */
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

    /**
     * Scheduled task that triggers a nightly synchronization of locker data.
     * <p>
     * This method runs based on a cron expression (daily at 03:00 AM).
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void onNightlySchedule() {
        log.info("CRON TRIGGERED: Starting nightly locker synchronization...");
        executeFullRefresh();
    }

    /**
     * Executes a full refresh of the locker cache.
     * <p>
     * This method fetches all locker data from the InPost API, processes and filters it,
     * updates the in-memory cache, and saves a new disk snapshot.
     * </p>
     */
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


    /**
     * Fetches the first page of locker data from the InPost API.
     *
     * @return an {@link InPostResponse} containing the first page of items and pagination metadata
     * @throws InPostApiException if there is an error communicating with the API
     */
    private InPostResponse fetchInitialPage() {
        String url = "https://api-global-points.easypack24.net/v1/points?page=1";
        try {
            return restTemplate.getForObject(url, InPostResponse.class);
        } catch (RestClientException e) {
            throw new InPostApiException("Failed to fetch the initial page from InPost API.", e);
        }
    }

    /**
     * Fetches all remaining pages of locker data concurrently.
     *
     * @param totalPages the total number of pages to fetch
     * @return a consolidated {@link List} of {@link Locker} objects from all remaining pages
     */
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

    /**
     * Fetches a single page of locker data from the InPost API with a retry mechanism.
     *
     * @param page the page number to fetch
     * @return a {@link List} of {@link Locker} objects from the page
     * @throws InPostApiException if the fetch fails after all retry attempts
     */
    private List<Locker> fetchPageSafely(int page) {
        String url = String.format("https://api-global-points.easypack24.net/v1/points?page=%d", page);
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);
                return (response != null && response.getItems() != null) ? response.getItems() : Collections.emptyList();
            } catch (RestClientException e) {
                attempt++;
                log.warn("Attempt {}/{} failed for page {}. Reason: {}", attempt, maxRetries, page, e.getMessage());

                if (attempt >= maxRetries) {
                    log.error("All {} attempts failed for page {}. Aborting fetch process.", maxRetries, page);
                    throw new InPostApiException("Failed to fetch page " + page + " after " + maxRetries + " attempts", e);
                }

                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new InPostApiException("Thread interrupted while retrying page " + page, ie);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Identifies names of machines that are marked as low-interest in the network.
     *
     * @param allLockers the full list of lockers
     * @return a {@link Set} of machine names that are considered low-interest
     */
    private Set<String> buildLowInterestSet(List<Locker> allLockers) {
        Set<String> lowInterestNamesSet = allLockers.stream()
                .filter(l -> l.getRecommendedLowInterestBoxMachinesList() != null)
                .flatMap(l -> l.getRecommendedLowInterestBoxMachinesList().stream())
                .collect(Collectors.toSet());

        log.info("Identified {} genuinely low-interest machines across the network.", lowInterestNamesSet.size());
        return lowInterestNamesSet;
    }

    /**
     * Filters, normalizes, and groups lockers into geospatial grids.
     *
     * @param allLockers           the list of all retrieved lockers
     * @param lowInterestNamesSet  a set of names for lockers to be marked as low-interest
     * @return a {@link Map} of grid identifiers to lists of processed {@link Locker} objects
     */
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

    /**
     * Normalizes the city name in the locker's address to title case.
     *
     * @param locker the {@link Locker} object to normalize
     * @return the same {@link Locker} object with a normalized city name
     */
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