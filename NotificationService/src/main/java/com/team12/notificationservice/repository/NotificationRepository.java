package com.team12.notificationservice.repository;

import com.team12.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByFromId(String fromId);
    List<Notification> findByToId(String toId);
}
