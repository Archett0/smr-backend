package com.team12.notificationservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team12.notificationservice.dto.NotificationDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.service.MessagingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@AllArgsConstructor
public class RabbitMQListener {

    private final MessagingService messagingService;

    @RabbitListener(queues = "notification.queue", concurrency = "5")
    public void receiveMessage(Notification notification) {
        try {
            NotificationDto notificationDTO = NotificationDto.builder()
                    .id(notification.getId())
                    .fromId(notification.getFromId())
                    .fromDeviceId(notification.getFromDeviceId())
                    .toId(notification.getToId())
                    .toDeviceId(notification.getToDeviceId())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .isRead(notification.isIsread())
                    .createdAt(notification.getCreatedAt())
                    .build();

            String toDeviceID = notification.getToDeviceId();
            String title = notification.getType().toString();
            String body = notification.getMessage();

            messagingService.sendNotificationToDevice(toDeviceID, title, body ,notificationDTO);

            log.info("Forwarded notification to user: {} with message: {}", notificationDTO.getToId(), notificationDTO);
        } catch (JsonProcessingException e) {
            log.error("Error while converting notification to JSON", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted", e);
        } catch (ExecutionException e) {
            log.error("Execution error", e);
        }
    }
}
