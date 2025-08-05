package com.team12.clients.notification.dto;

public record NotificationRequest(
        String toUserId,
        String message,
        NotificationType type
){
}
