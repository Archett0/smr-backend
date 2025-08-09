package com.team12.notificationservice.service;

import com.team12.clients.notification.dto.NotificationRequest;
import com.team12.clients.user.UserClient;
import com.team12.notificationservice.dto.NotificationCreateDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.model.NotificationType;
import com.team12.notificationservice.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final AmqpTemplate amqpTemplate;

    private static final String ERR_NOT_FOUND_FMT = "Notification not found: %s";

    private Notification buildNotification(
            String fromId,
            String fromDeviceId,
            String toId,
            String toDeviceId,
            String message,
            NotificationType type
    ) {
        return Notification.builder()
                .fromId(fromId)
                .fromDeviceId(fromDeviceId)
                .toId(toId)
                .toDeviceId(toDeviceId)
                .message(message)
                .type(type)
                .isread(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Notification saveAndPublish(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        try {
            amqpTemplate.convertAndSend(
                    "notification.exchange",
                    "notification.routing.key",
                    saved
            );
            log.info("Notification[{}] published to toId={}", saved.getId(), saved.getToId());
        } catch (Exception e) {
            log.error("Failed to publish Notification[{}]", saved.getId(), e);
        }
        return saved;
    }

    @Transactional
    public Notification createNotification(NotificationCreateDto request) {
        String toDeviceId = userClient.getDeviceIDById(Long.parseLong(request.getToId())).getBody();
        String fromDeviceId = userClient.getDeviceIDById(Long.parseLong(request.getFromId())).getBody();

        Notification n = buildNotification(
                request.getFromId(),
                fromDeviceId,
                request.getToId(),
                toDeviceId,
                request.getMessage(),
                request.getType()
        );
        return saveAndPublish(n);
    }

    @Transactional
    public void sendNotification(NotificationRequest dto) {
        String toDeviceId = userClient.getDeviceIDById(Long.parseLong(dto.toUserId())).getBody();
        Notification n = buildNotification(
                "1",
                "System",
                dto.toUserId(),
                toDeviceId,
                dto.message(),
                NotificationType.SYSTEM
        );
        saveAndPublish(n);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getByFromId(String fromId) {
        return notificationRepository.findByFromId(fromId);
    }

    public List<Notification> getByToId(String toId) {
        return notificationRepository.findByToId(toId);
    }

    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(Long id) {
        markAsReadInternal(id);
    }

    @Transactional
    public Notification acceptNotification(Long id) {
        Notification reply = buildReplyForApplication(id, true);
        Notification saved = saveAndPublish(reply);
        markAsReadInternal(id);
        return saved;
    }

    @Transactional
    public Notification denyNotification(Long id) {
        Notification reply = buildReplyForApplication(id, false);
        Notification saved = saveAndPublish(reply);
        markAsReadInternal(id);
        return saved;
    }

    private void markAsReadInternal(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ERR_NOT_FOUND_FMT.formatted(id)));
        n.setIsread(true);
        notificationRepository.save(n);
    }

    private Notification buildReplyForApplication(Long id, boolean action) {
        Notification orig = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ERR_NOT_FOUND_FMT.formatted(id)));

        String fullMsg = orig.getMessage();
        Pattern p = Pattern.compile(
                "requested a viewing for\\s+(\\d+)\\s+at\\s+(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(fullMsg);
        String details;
        if (m.find()) {
            String ten = m.group(1);
            String timestamp = m.group(2);
            details = String.format("viewing for %s at %s", ten, timestamp);
        } else {
            details = "viewing";
        }

        String replyMsg = action
                ? String.format("Agent %s passed your %s", orig.getFromId(), details)
                : String.format("Agent %s denyed your %s", orig.getFromId(), details);

        return buildNotification(
                orig.getToId(),
                orig.getToDeviceId(),
                orig.getFromId(),
                orig.getFromDeviceId(),
                replyMsg,
                NotificationType.SYSTEM
        );
    }
}
