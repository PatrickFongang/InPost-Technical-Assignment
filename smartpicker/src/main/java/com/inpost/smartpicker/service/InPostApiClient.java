package com.inpost.smartpicker.service;

import com.inpost.smartpicker.exception.InPostApiException;
import com.inpost.smartpicker.model.InPostResponse;
import com.inpost.smartpicker.model.Locker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InPostApiClient {
    private final RestTemplate restTemplate;
    private final ExecutorService ioExecutor;

    @Cacheable(value = "lockersByCity", key = "#city.toLowerCase()")
    public List<Locker> fetchLockersByCity(String city) {
        log.info("No cache found. Fetching lockers for city: {}", city);

        String firstPageUrl = String.format("https://api-global-points.easypack24.net/v1/points?city=%s&page=1", city);
        InPostResponse firstResponse;

        try {
            firstResponse = restTemplate.getForObject(firstPageUrl, InPostResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch initial page for city {}: {}", city, e.getMessage());
            throw new InPostApiException("Failed to connect to InPost API");
        }

        if (firstResponse == null || firstResponse.getItems() == null || firstResponse.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<Locker> allLockers = new ArrayList<>(firstResponse.getItems());
        Integer totalPages = firstResponse.getTotalPages();

        if (totalPages == null || totalPages <= 1) {
            return allLockers;
        }

        log.info("City {} has {} pages. Firing parallel requests...", city, totalPages);

        List<CompletableFuture<List<Locker>>> futures = new ArrayList<>();

        for (int i = 2; i <= totalPages; i++) {
            final int page = i;
            CompletableFuture<List<Locker>> future = CompletableFuture.supplyAsync(() -> {
                String url = String.format("https://api-global-points.easypack24.net/v1/points?city=%s&page=%d", city, page);
                try {
                    InPostResponse response = restTemplate.getForObject(url, InPostResponse.class);
                    return (response != null && response.getItems() != null) ? response.getItems() : Collections.emptyList();
                } catch (Exception e) {
                    log.error("Failed to fetch page {} for city {}", page, city);
                    return Collections.emptyList();
                }
            },ioExecutor);
            futures.add(future);
        }

        List<Locker> remainingLockers = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        allLockers.addAll(remainingLockers);
        log.info("Successfully fetched {} lockers across {} pages concurrently.", allLockers.size(), totalPages);

        return allLockers;
    }
}
