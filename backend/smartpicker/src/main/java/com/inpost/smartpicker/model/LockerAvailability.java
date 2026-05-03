package com.inpost.smartpicker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockerAvailability {
    private String status;
    private Map<String, String> details;
}
