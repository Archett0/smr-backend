package com.team12.recommendationservice.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class GeoLocation {
    public Double latitude;
    public Double longitude;
}
