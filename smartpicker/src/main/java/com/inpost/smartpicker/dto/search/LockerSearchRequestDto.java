package com.inpost.smartpicker.dto.search;

public record LockerSearchRequestDto(
        String city,
        Double userLat,
        Double userLon,
        Double radiusInKm,
        Boolean stressFreeMode,
        Boolean thermoMode

) {
    public LockerSearchRequestDto {
        if (radiusInKm == null) radiusInKm = 2.0;
        if (stressFreeMode == null) stressFreeMode = false;
        if (thermoMode == null) thermoMode = false;
    }
}
