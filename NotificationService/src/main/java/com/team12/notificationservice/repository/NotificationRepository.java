package com.team12.notificationservice.repository;

import com.team12.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTenantId(String tenantId);
    List<Notification> findByAgentId(String agentId);
}
