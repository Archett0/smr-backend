package com.team12.recommendationservice.model;

import java.time.LocalDateTime;


public class UserAction {

    private Long id;

    private Long userId;

    public Long listingId;

    private int actionValue;

    private LocalDateTime createdAt  = LocalDateTime.now();
}