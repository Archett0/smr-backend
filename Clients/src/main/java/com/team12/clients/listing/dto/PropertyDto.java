package com.team12.clients.listing.dto;

import java.io.Serializable;

public class PropertyDto implements Serializable {
    private Property property;
    private String username;
    private String phoneNumber;

    // Getters and Setters
    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}