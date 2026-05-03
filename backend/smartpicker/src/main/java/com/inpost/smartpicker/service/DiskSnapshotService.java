package com.inpost.smartpicker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inpost.smartpicker.model.Locker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiskSnapshotService {

    private final ObjectMapper objectMapper;
    private static final String SNAPSHOT_FILE_PATH = "lockers_snapshot.json";

    public void saveSnapshot(Map<String, List<Locker>> cache) {
        log.info("Saving in-memory grid to local disk snapshot...");
        long startTime = System.currentTimeMillis();
        try {
            File file = new File(SNAPSHOT_FILE_PATH);
            objectMapper.writeValue(file, cache);

            long duration = System.currentTimeMillis() - startTime;
            long sizeMb = file.length() / (1024 * 1024);
            log.info("Snapshot successfully saved to disk in {} ms. File size: {} MB.", duration, sizeMb);

        } catch (IOException e) {
            log.error("Failed to save local snapshot to disk: {}", e.getMessage());
        }
    }

    public Map<String, List<Locker>> loadSnapshot() {
        File file = new File(SNAPSHOT_FILE_PATH);
        if (!file.exists()) {
            log.info("No local snapshot found at [{}]. Returning empty state.", SNAPSHOT_FILE_PATH);
            return null;
        }

        try {
            log.info("Found local disk snapshot. Loading data into RAM...");
            long startTime = System.currentTimeMillis();

            Map<String, List<Locker>> cache = objectMapper.readValue(file, new TypeReference<Map<String, List<Locker>>>() {});

            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully loaded {} geospatial grids from disk in {} ms.", cache.size(), duration);
            return cache;

        } catch (IOException e) {
            log.error("Failed to load snapshot from disk (file might be corrupted). Proceeding with empty state: {}", e.getMessage());
            return null;
        }
    }
}