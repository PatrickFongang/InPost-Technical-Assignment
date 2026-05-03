package com.inpost.smartpicker.dto.weather;

public record WeatherInfoDto(
        boolean isExtreme,
        Double minTemp,
        Double maxTemp
) {}