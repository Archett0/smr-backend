package com.team12.notificationservice.service;

import com.team12.clients.notification.dto.NotificationRequest;
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
    private final AmqpTemplate           amqpTemplate;

    private Notification buildNotification(
            String fromId,
            String toId,
            String message,
            NotificationType type
    ) {
        return Notification.builder()
                .fromId(fromId)
                .toId(toId)
                .message(message)
                .type(type)
                .isread(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    protected Notification saveAndPublish(Notification notification) {
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

    /** 创建新的通知 */
    public Notification createNotification(Notification request) {
        Notification n = buildNotification(
                request.getFromId(),
                request.getToId(),
                request.getMessage(),
                request.getType()
        );
        return saveAndPublish(n);
    }

    /** 接收来自其他微服务的通知请求 */
    public Notification sendNotification(NotificationRequest dto) {
        Notification n = buildNotification(
                "1",
                dto.toUserId(),
                dto.message(),
                NotificationType.SYSTEM
        );
        return saveAndPublish(n);
    }

    /** 查询所有通知 */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /** 按 ID 查询通知 */
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    /** 按发送者 ID 查询 */
    public List<Notification> getByFromId(String fromId) {
        return notificationRepository.findByFromId(fromId);
    }

    /** 按接收者 ID 查询 */
    public List<Notification> getByToId(String toId) {
        return notificationRepository.findByToId(toId);
    }

    /** 更新通知（仅持久化修改） */
    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /** 标记某条通知为已读 */
    @Transactional
    public void deleteNotification(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
        n.setIsread(true);
        notificationRepository.save(n);
    }

    public Notification acceptNotification(Long id) {
        Notification reply = handleApplication(id, true);
        return saveAndPublish(reply);
    }

    public Notification denyNotification(Long id) {
        Notification reply = handleApplication(id, false);
        return saveAndPublish(reply);
    }

    public Notification handleApplication(Long id, boolean action) {
        Notification orig = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));

        String fullMsg = orig.getMessage();
        Pattern p = Pattern.compile(
                "requested a viewing for\\s+(\\d+)\\s+at\\s+(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(fullMsg);
        String details;
        if (m.find()) {
            String ten  = m.group(1);
            String timestamp = m.group(2);
            details = String.format("viewing for %s at %s", ten, timestamp);
        } else {
            details = "viewing";
        }


        String replyMsg = "";
        if(action){
            replyMsg = String.format("Agent %s passed your %s",
                    orig.getFromId(), details);
        }else{
            replyMsg = String.format("Agent %s denyed your %s",
                    orig.getFromId(), details);
        }

        Notification reply = buildNotification(
                orig.getToId(),
                orig.getFromId(),
                replyMsg,
                NotificationType.SYSTEM
        );
        deleteNotification(orig.getId());
        return saveAndPublish(reply);
    }


}
