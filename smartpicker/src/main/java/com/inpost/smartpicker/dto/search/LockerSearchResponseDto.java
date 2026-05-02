package com.inpost.smartpicker.dto.search;

import com.inpost.smartpicker.dto.weather.WeatherInfoDto;
import com.inpost.smartpicker.model.Locker;
import java.util.List;

public record LockerSearchResponseDto(
        List<Locker> lockers,
        WeatherInfoDto weatherInfo
) {}