package com.team12.clients.listing.dto;

import java.io.Serializable;

public class GeoLocation implements Serializable {
    private Double latitude;
    private Double longitude;

    // Getters and Setters
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
