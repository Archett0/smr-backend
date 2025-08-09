package com.team12.recommendationservice.model;

import java.time.LocalDateTime;


public class UserAction {

    private Long id;

    private Long userId;

    private Long listingId;

    private int actionValue;

    private LocalDateTime createdAt  = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getListingId() {
        return listingId;
    }

    public int getActionValue() {
        return actionValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}