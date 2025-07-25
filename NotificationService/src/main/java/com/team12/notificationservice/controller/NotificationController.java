package com.team12.notificationservice.controller;

import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification Controller APIs", description = "CRUD for notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<Notification> getById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get notifications by tenant ID")
    public ResponseEntity<List<Notification>> getByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(notificationService.getByTenantId(tenantId));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get notifications by agent ID")
    public ResponseEntity<List<Notification>> getByAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(notificationService.getByAgentId(agentId));
    }

    @PutMapping
    @Operation(summary = "Update a notification")
    public ResponseEntity<Notification> update(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.updateNotification(notification));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification by ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
