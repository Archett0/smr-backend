package com.team12.listingservice.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class GeoLocation {
    private Double latitude;
    private Double longitude;
}
