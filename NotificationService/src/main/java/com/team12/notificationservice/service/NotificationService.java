package com.team12.notificationservice.service;

import com.team12.notificationservice.dto.NotificationDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AmqpTemplate amqpTemplate;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

//    public Notification createNotification(Notification notification) {
//        notification.setCreatedAt(LocalDateTime.now());
//        notification.setIsread(false);
//        return notificationRepository.save(notification);
//    }

    public Notification createNotification(Notification request) {
        Notification notification = Notification.builder()
                .tenantId(request.getTenantId())
                .agentId(request.getAgentId())
                .message(request.getMessage())
                .type(request.getType())
                .isread(false)
                .createdAt(LocalDateTime.now())
                .build();

        executorService.submit(() -> {
            amqpTemplate.convertAndSend("notification.exchange", "notification.routing.key", notification);

            log.info("Notification sent and saved to database for tenant: {}", request.getTenantId());
        });

        return notificationRepository.save(notification);
    }


    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getByTenantId(String tenantId) {
        return notificationRepository.findByTenantId(tenantId);
    }

    public List<Notification> getByAgentId(String agentId) {
        return notificationRepository.findByAgentId(agentId);
    }

    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
        notification.setIsread(true);
        notificationRepository.save(notification);
    }
}
