package com.inpost.smartpicker.dto.error;

import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record ErrorResponseDto (
        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path
) {}
