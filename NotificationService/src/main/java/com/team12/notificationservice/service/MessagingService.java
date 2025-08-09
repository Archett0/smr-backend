package com.team12.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.team12.notificationservice.dto.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
public class MessagingService {

    private final ObjectMapper objectMapper;

    public void sendNotificationToDevice(String deviceToken, String title, String body, NotificationDto notificationDto)
            throws InterruptedException, ExecutionException, JsonProcessingException {

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        String notificationJson = objectMapper.writeValueAsString(notificationDto);

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .putData("data", notificationJson)
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        log.info("Successfully sent message: {}", response);
    }
}