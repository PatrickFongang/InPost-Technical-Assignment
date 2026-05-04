package com.inpost.smartpicker.dto.weather;

public record WeatherInfoDto(
        Boolean isExtreme,
        Double minTemp,
        Double maxTemp
) {}