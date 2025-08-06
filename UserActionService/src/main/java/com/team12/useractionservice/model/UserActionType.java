package com.team12.useractionservice.model;

public enum UserActionType {
    FAVORITE(1),    // like
    UNFAVORITE(-1);  // ccancel like

    private final int value;

    UserActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}