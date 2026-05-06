package com.inpost.smartpicker.controller;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.dto.search.LockerSearchResponseDto;
import com.inpost.smartpicker.service.InPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/lockers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LockerController {

    private final InPostService inPostService;

    /**
     * Searches for the nearest lockers based on the provided search criteria.
     * <p>
     * This endpoint accepts a JSON request body containing user location, search radius,
     * and optional filters such as thermal mode and expected delivery date.
     * </p>
     *
     * @param request the {@link LockerSearchRequestDto} containing the search parameters
     * @return a {@link ResponseEntity} containing the {@link LockerSearchResponseDto} with matching lockers and weather info
     */
    @PostMapping("/search")
    public ResponseEntity<LockerSearchResponseDto> searchNearestLockers(@Valid @RequestBody LockerSearchRequestDto request) {
        log.info("Received locker search request with parameters: {}", request);

        LockerSearchResponseDto results = inPostService.searchLockers(request);

        return ResponseEntity.ok(results);
    }
}