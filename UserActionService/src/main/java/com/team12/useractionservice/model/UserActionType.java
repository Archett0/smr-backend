package com.team12.useractionservice.model;

public enum UserActionType {
    FAVORITE(1),    // like
    UNFAVORITE(-1),  // ccancel like
    PRICE_ALERT(2),          // OPEN PRICE ALERT
    CANCEL_PRICE_ALERT(-2);  // CANCEL PRICE ALERT

    private final int value;

    UserActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}