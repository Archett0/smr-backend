package com.team12.notificationservice;

import com.team12.clients.notification.dto.NotificationRequest;
import com.team12.clients.user.UserClient;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.model.NotificationType;
import com.team12.notificationservice.repository.NotificationRepository;
import com.team12.notificationservice.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repo;
    @Mock UserClient userClient;
    @Mock AmqpTemplate amqp;

    @InjectMocks NotificationService service;

    private final AtomicLong idGen = new AtomicLong(1);

    private Notification origAppRequest(Long id, String msg) {
        return Notification.builder()
                .id(id)
                .fromId("agentA")
                .fromDeviceId("devA")
                .toId("tenantT")
                .toDeviceId("devT")
                .message(msg)
                .type(NotificationType.SYSTEM)
                .isread(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setup() {
        idGen.set(1);
        lenient().when(repo.save(Mockito.<Notification>any()))
                .thenAnswer(inv -> {
                    Notification n = inv.getArgument(0, Notification.class);
                    if (n == null) return null;
                    if (n.getId() == null) n.setId(idGen.getAndIncrement());
                    return n;
                });
    }

    @Test
    @DisplayName("createNotification: successfully store and save")
    void createNotification_ok() {
        var dto = mock(com.team12.notificationservice.dto.NotificationCreateDto.class);
        when(dto.getFromId()).thenReturn("10");
        when(dto.getToId()).thenReturn("20");
        when(dto.getMessage()).thenReturn("hello");
        when(dto.getType()).thenReturn(NotificationType.SYSTEM);

        when(userClient.getDeviceIDById(10L)).thenReturn(ResponseEntity.ok("dev-from-10"));
        when(userClient.getDeviceIDById(20L)).thenReturn(ResponseEntity.ok("dev-to-20"));

        Notification saved = service.createNotification(dto);

        assertNotNull(saved.getId());
        assertEquals("10", saved.getFromId());
        assertEquals("20", saved.getToId());
        assertEquals("dev-from-10", saved.getFromDeviceId());
        assertEquals("dev-to-20", saved.getToDeviceId());

        verify(amqp, atLeastOnce()).convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                Mockito.<Object>any()
        );
    }

    @Test
    @DisplayName("createNotification: AMQP send fail")
    void createNotification_publish_fail_still_ok() {
        var dto = mock(com.team12.notificationservice.dto.NotificationCreateDto.class);
        when(dto.getFromId()).thenReturn("10");
        when(dto.getToId()).thenReturn("20");
        when(dto.getMessage()).thenReturn("hello");
        when(dto.getType()).thenReturn(NotificationType.SYSTEM);

        when(userClient.getDeviceIDById(10L)).thenReturn(ResponseEntity.ok("dev-from-10"));
        when(userClient.getDeviceIDById(20L)).thenReturn(ResponseEntity.ok("dev-to-20"));

        doThrow(new RuntimeException("mq down")).when(amqp)
                .convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                Mockito.<Object>any());

        Notification saved = service.createNotification(dto);

        assertNotNull(saved.getId());
        assertEquals("20", saved.getToId());
        verify(repo, atLeastOnce()).save(any());
        verify(amqp, atLeastOnce()).convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                Mockito.<Object>any()
        );
    }


    @Test
    @DisplayName("sendNotification: store and save（SYSTEM）")
    void sendNotification_ok() {
        NotificationRequest req = mock(NotificationRequest.class);
        when(req.toUserId()).thenReturn("20");
        when(req.message()).thenReturn("sys msg");
        when(userClient.getDeviceIDById(20L)).thenReturn(ResponseEntity.ok("dev-to-20"));

        service.sendNotification(req);

        verify(repo, atLeastOnce()).save(Mockito.<Notification>any());
        verify(amqp, atLeastOnce()).convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                Mockito.<Object>any()
        );
    }

    @Test
    @DisplayName("acceptNotification: no record -> EntityNotFoundException")
    void accept_not_found() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.acceptNotification(999L));
    }

    @Test
    @DisplayName("denyNotification: no record -> EntityNotFoundException")
    void deny_not_found() {
        when(repo.findById(998L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.denyNotification(998L));
    }

    @Test
    @DisplayName("acceptNotification: Regular case insensitivity (CASE_INSENSITIVE)")
    void accept_regex_case_insensitive() {
        String msg = "ReQueStEd A VieWing For 777 at 2025-12-31 08:09";
        Notification orig = Notification.builder()
                .id(66L).fromId("agentA").fromDeviceId("devA")
                .toId("tenantT").toDeviceId("devT")
                .message(msg).type(NotificationType.SYSTEM)
                .isread(false).createdAt(LocalDateTime.now()).build();

        when(repo.findById(66L)).thenReturn(Optional.of(orig), Optional.of(orig));

        Notification reply = service.acceptNotification(66L);
        assertTrue(reply.getMessage().contains("viewing for 777 at 2025-12-31 08:09"));
    }



    @Test
    @DisplayName("CRUD & search：getAll/getById/getByFromId/getByToId/update")
    void queries_and_update() {
        Notification n = origAppRequest(null, "m");
        when(repo.findAll()).thenReturn(List.of(n));
        when(repo.findById(1L)).thenReturn(Optional.of(n));
        when(repo.findByFromId("agentA")).thenReturn(List.of(n));
        when(repo.findByToId("tenantT")).thenReturn(List.of(n));

        assertEquals(1, service.getAllNotifications().size());
        assertTrue(service.getNotificationById(1L).isPresent());
        assertEquals(1, service.getByFromId("agentA").size());
        assertEquals(1, service.getByToId("tenantT").size());

        Notification updated = Notification.builder()
                .id(99L).fromId("x").toId("y").message("u")
                .type(NotificationType.SYSTEM).isread(true).createdAt(LocalDateTime.now()).build();

        when(repo.save(Mockito.<Notification>any())).thenReturn(updated);

        Notification ret = service.updateNotification(updated);
        assertEquals(99L, ret.getId());
    }

    @Test
    @DisplayName("deleteNotification: notification is read")
    void delete_marks_read() {
        Notification n = origAppRequest(5L, "m");
        when(repo.findById(5L)).thenReturn(Optional.of(n));

        service.deleteNotification(5L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo, atLeast(1)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(Notification::isIsread));
    }

    @Test
    @DisplayName("deleteNotification: error EntityNotFoundException")
    void delete_not_found() {
        when(repo.findById(404L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteNotification(404L));
    }

    @Test
    @DisplayName("acceptNotification: Regular matching version (including tenant ID and time)")
    void accept_regex_match() {
        String msg = "requested a viewing for 123 at 2025-08-01 14:30";
        Notification orig = origAppRequest(7L, msg);

        when(repo.findById(7L)).thenReturn(Optional.of(orig), Optional.of(orig));

        Notification reply = service.acceptNotification(7L);

        assertNotNull(reply.getId());
        assertEquals("tenantT", reply.getFromId());
        assertEquals("agentA", reply.getToId());
        assertEquals("Agent agentA passed your viewing for 123 at 2025-08-01 14:30", reply.getMessage());

        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo).save(argThat(n -> n != null && n.getMessage().startsWith("Agent agentA passed")));
        inOrder.verify(repo).save(argThat(Notification::isIsread));
    }

    @Test
    @DisplayName("denyNotification: Regular mismatch version (fallback 'viewing', and keep the source code' denyed')")
    void deny_regex_not_match() {
        String msg = "no key phrases here";
        Notification orig = origAppRequest(8L, msg);

        when(repo.findById(8L)).thenReturn(Optional.of(orig), Optional.of(orig));

        Notification reply = service.denyNotification(8L);

        assertNotNull(reply.getId());
        assertEquals("Agent agentA denyed your viewing", reply.getMessage());
    }
}
