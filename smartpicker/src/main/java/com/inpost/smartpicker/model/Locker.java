package com.inpost.smartpicker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Locker {
    private String name;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("easy_access_zone")
    private boolean easyAccessZone;

    @JsonProperty("recommended_low_interest_box_machines_list")
    private List<String> recommendedLowInterestBoxMachinesList;

    private Location location;

    @JsonProperty("address_details")
    private AddressDetails addressDetails;

    private Double distance;
}
