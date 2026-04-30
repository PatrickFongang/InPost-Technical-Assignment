package com.inpost.smartpicker.controller;

import com.inpost.smartpicker.dto.search.LockerSearchRequestDto;
import com.inpost.smartpicker.model.Locker;
import com.inpost.smartpicker.service.InPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lockers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LockerController {

    private final InPostService inPostService;

    @GetMapping("/search")
    public ResponseEntity<List<Locker>> searchNearestLockers(LockerSearchRequestDto request) {
        log.info("Odebrano żądanie wyszukiwania paczkomatów z parametrami: {}", request);

        List<Locker> results = inPostService.searchLockers(request);

        return ResponseEntity.ok(results);
    }
}