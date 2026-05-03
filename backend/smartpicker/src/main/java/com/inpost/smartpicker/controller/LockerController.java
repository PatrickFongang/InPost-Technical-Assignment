package com.inpost.smartpicker.controller;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.dto.search.LockerSearchResponseDto;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.service.InPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lockers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LockerController {

    private final InPostService inPostService;

    @GetMapping("/search")
    public ResponseEntity<LockerSearchResponseDto> searchNearestLockers(@Valid @RequestBody LockerSearchRequestDto request) {
        log.info("Received locker search request with parameters: {}", request);

        LockerSearchResponseDto results = inPostService.searchLockers(request);

        return ResponseEntity.ok(results);
    }
}