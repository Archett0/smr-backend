package com.team12.notificationservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.notificationservice.dto.NotificationDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.service.MessagingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class RabbitMQListener {

    private final MessagingService messagingService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.queue", concurrency = "5")
    public void receiveMessage(Notification notification) {
        try {
            NotificationDto notificationDTO = NotificationDto.builder()
                    .id(notification.getId())
                    .tenantId(notification.getTenantId())
                    .agentId(notification.getAgentId())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .isRead(notification.isIsread())
                    .createdAt(notification.getCreatedAt())
                    .build();

            String notificationJson = objectMapper.writeValueAsString(notificationDTO);

            String userId = notification.getTenantId();

            messagingService.sendMessageToUser(userId, notificationJson);

            log.info("Forwarded notification to user: {} with message: {}", userId, notificationJson);
        } catch (IllegalArgumentException e) {
            log.error("Invalid String format for agentId: {}", notification.getAgentId());
        } catch (JsonProcessingException e) {
            log.error("Error while converting notification to JSON", e);
        }
    }
}
