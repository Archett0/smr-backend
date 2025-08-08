package com.team12.listingservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class GeoLocation {
    private Double latitude;
    private Double longitude;
}
