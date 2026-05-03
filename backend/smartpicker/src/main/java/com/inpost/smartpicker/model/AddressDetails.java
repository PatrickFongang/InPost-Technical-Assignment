package com.inpost.smartpicker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDetails {
    private String city;
    private String street;
    @JsonProperty("building_number")
    private String buildingNumber;
}
