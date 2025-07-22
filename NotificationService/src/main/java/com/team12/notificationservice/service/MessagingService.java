package com.team12.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessageToUser(String userId, String message) {
        String destination = "/user/" + userId + "/notification";
        messagingTemplate.convertAndSend(destination, message);
    }
}