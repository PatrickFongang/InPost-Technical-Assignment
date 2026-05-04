package com.inpost.smartpicker.dto.search;

import com.inpost.smartpicker.validation.ValidDeliveryDate;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record LockerSearchRequestDto(
        @NotNull(message = "Latitude (userLat) is required")
        @Min(value = -90, message = "Latitude cannot be less than -90")
        @Max(value = 90, message = "Latitude cannot be greater than 90")
        Double userLat,
        @NotNull(message = "Longitude (userLon) is required")
        @Min(value = -180, message = "Longitude cannot be less than -180")
        @Max(value = 180, message = "Longitude cannot be greater than 180")
        Double userLon,
        @Positive(message = "Search radius must be a positive number")
        @Max(value = 20, message = "Maximum supported search radius is 20 km")
        Double radiusInKm,
        //@ValidDeliveryDate
        LocalDate expectedDeliveryDate,
        Boolean thermoMode
) {
    public LockerSearchRequestDto {
        if (radiusInKm == null) radiusInKm = 2.0;
        if (thermoMode == null) thermoMode = false;
    }
}
