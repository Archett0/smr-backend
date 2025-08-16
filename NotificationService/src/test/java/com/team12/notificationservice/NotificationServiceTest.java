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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    @DisplayName("createNotification: should successfully store and save notification")
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
    @DisplayName("createNotification: should still succeed when AMQP send fails")
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
    @DisplayName("sendNotification: should store and save (SYSTEM)")
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
    @DisplayName("acceptNotification: should throw EntityNotFoundException if not found")
    void accept_not_found() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.acceptNotification(999L));
    }

    @Test
    @DisplayName("denyNotification: should throw EntityNotFoundException if not found")
    void deny_not_found() {
        when(repo.findById(998L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.denyNotification(998L));
    }

    @Test
    @DisplayName("acceptNotification: should be case-insensitive in regex match")
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
    @DisplayName("CRUD & search: getAll/getById/getByFromId/getByToId/update")
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
    @DisplayName("deleteNotification: should mark notification as read")
    void delete_marks_read() {
        Notification n = origAppRequest(5L, "m");
        when(repo.findById(5L)).thenReturn(Optional.of(n));

        service.deleteNotification(5L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo, atLeast(1)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(Notification::isIsread));
    }

    @Test
    @DisplayName("deleteNotification: should throw EntityNotFoundException if not found")
    void delete_not_found() {
        when(repo.findById(404L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteNotification(404L));
    }

    @Test
    @DisplayName("acceptNotification: should match regex and parse tenant ID and time")
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
    @DisplayName("denyNotification: should fallback to default 'viewing' text when regex does not match")
    void deny_regex_not_match() {
        String msg = "no key phrases here";
        Notification orig = origAppRequest(8L, msg);

        when(repo.findById(8L)).thenReturn(Optional.of(orig), Optional.of(orig));

        Notification reply = service.denyNotification(8L);

        assertNotNull(reply.getId());
        assertEquals("Agent agentA denyed your viewing", reply.getMessage());
    }

    @Test
    @DisplayName("createNotification: should keep original message and type, and set createdAt")
    void createNotification_keeps_message_type_and_sets_createdAt() {
        var dto = mock(com.team12.notificationservice.dto.NotificationCreateDto.class);
        when(dto.getFromId()).thenReturn("10");
        when(dto.getToId()).thenReturn("20");
        when(dto.getMessage()).thenReturn("hi-hi");
        when(dto.getType()).thenReturn(NotificationType.SYSTEM);

        when(userClient.getDeviceIDById(10L)).thenReturn(ResponseEntity.ok("df"));
        when(userClient.getDeviceIDById(20L)).thenReturn(ResponseEntity.ok("dt"));

        Notification saved = service.createNotification(dto);

        assertEquals("hi-hi", saved.getMessage());
        assertEquals(NotificationType.SYSTEM, saved.getType());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("sendNotification: should query recipient device and publish to fixed exchange/routing key")
    void sendNotification_invokes_userClient_and_amqp() {
        var req = mock(NotificationRequest.class);
        when(req.toUserId()).thenReturn("42");
        when(req.message()).thenReturn("ping");
        when(userClient.getDeviceIDById(42L)).thenReturn(ResponseEntity.ok("dev-42"));

        service.sendNotification(req);

        verify(userClient).getDeviceIDById(42L);
        verify(amqp, atLeastOnce()).convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                Mockito.<Object>any()
        );
    }

    @Test
    @DisplayName("acceptNotification: should fallback when message contains excessive spaces or mixed case")
    void accept_regex_with_extra_spaces() {
        // This message contains excessive spaces; current regex won't match and should fallback
        String msg = "  requested   a   viewing   for   321   at  2026-02-02 09:08 ";
        Notification orig = origAppRequest(200L, msg);
        when(repo.findById(200L)).thenReturn(Optional.of(orig), Optional.of(orig));

        Notification reply = service.acceptNotification(200L);

        String lower = reply.getMessage().toLowerCase();
        assertTrue(lower.contains("passed your viewing"),
                "When unable to parse ID/time, fallback text should be returned");
    }

    // ===========================
    // ▼▼▼ Parameterized bulk regex cases ▼▼▼
    // ===========================

    static Stream<org.junit.jupiter.params.provider.Arguments> validMessages() {
        String[] ids = {"1", "12", "123", "456", "7890", "321"};
        String[] times = {
                "2025-01-01 08:00", "2025-05-31 23:59",
                "2026-02-02 09:08", "2027-12-12 12:12",
                "2030-10-10 10:10", "2025-08-01 14:30"
        };
        String[] templates = {
                "requested a viewing for %s at %s",
                "Requested a viewing for %s at %s",
                "reQUESteD a ViEWinG for %s at %s"
        };
        return IntStream.range(0, ids.length).boxed().flatMap(i ->
                IntStream.range(0, times.length).boxed().flatMap(j ->
                        Stream.of(
                                org.junit.jupiter.params.provider.Arguments.of(
                                        String.format(templates[0], ids[i], times[j]),
                                        ids[i], times[j]),
                                org.junit.jupiter.params.provider.Arguments.of(
                                        String.format(templates[1], ids[i], times[j]),
                                        ids[i], times[j]),
                                org.junit.jupiter.params.provider.Arguments.of(
                                        String.format(templates[2], ids[i], times[j]),
                                        ids[i], times[j])
                        )
                )
        ).limit(36);
    }

    static Stream<String> invalidMessages() {
        return Stream.of(
                "requested a viewing for A12 at 2025-01-01 10:00",
                "requested a viewing for 12 at 2025/01/01 10:00",
                "request a viewing for 12 at 2025-01-01 10:00",
                "requested viewing for 12 at 2025-01-01 10:00",
                "requested a booking for 12 at 2025-01-01 10:00",
                "requested a viewing for 12 at 25-01-01 10:00",
                "requested a viewing for 12 at 2025-13-01 10:00",
                "requested a viewing for 12 at 2025-01-32 10:00",
                "requested  a   viewing for 12  2025-01-01 10:00",
                "requested a  viewing for at 2025-01-01 10:00",
                "viewing for 12 at 2025-01-01 10:00",
                "requested a viewing for 12",
                "requested a viewing at 2025-01-01 10:00",
                "  requested   a   booking   ",
                "foobar",
                "REQUESTED A VIEWING FOR  at 2025-01-01 10:00",
                "requested a viewing for 12 at 2025-01-01",
                "requested a viewing for 12 at 10:00",
                "Requested A Viewing For 12 At 2025-01-01 10:00 ",
                " requested a viewing for 12 at  2025-01-01 10:00 "
        );
    }

    @ParameterizedTest(name = "[Accept OK #{index}] {0}")
    @MethodSource("validMessages")
    @DisplayName("acceptNotification bulk: should parse ID/time and mark original as read")
    void accept_ok_many(String msg, String expectId, String expectTime) {
        Notification orig = origAppRequest(100L, msg);
        when(repo.findById(100L)).thenReturn(Optional.of(orig), Optional.of(orig));
        // Important: do not override save() stub from @BeforeEach, so IDs are auto-filled

        Notification reply = service.acceptNotification(100L);

        assertNotNull(reply.getId());
        String m = reply.getMessage();
        assertTrue(m.contains("passed your viewing for " + expectId + " at " + expectTime),
                "Should contain parsed ID and time: " + m);

        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo).save(argThat(n -> n.getMessage() != null && n.getMessage().startsWith("Agent agentA passed")));
        inOrder.verify(repo).save(argThat(Notification::isIsread));
    }

    @ParameterizedTest(name = "[Accept Fallback #{index}] {0}")
    @MethodSource("invalidMessages")
    @DisplayName("acceptNotification bulk: should fallback when parsing fails and mark original as read")
    void accept_fallback_many(String msg) {
        Notification orig = origAppRequest(200L, msg);
        when(repo.findById(200L)).thenReturn(Optional.of(orig), Optional.of(orig));
        // Important: do not override save() stub from @BeforeEach, so IDs are auto-filled

        Notification reply = service.acceptNotification(200L);

        String lower = reply.getMessage().toLowerCase();
        assertTrue(lower.contains("passed your viewing"),
                "Should return fallback text when parsing fails: " + reply.getMessage());

        ArgumentCaptor<Notification> cap = ArgumentCaptor.forClass(Notification.class);
        verify(repo, atLeast(2)).save(cap.capture());
        assertTrue(cap.getAllValues().stream().anyMatch(Notification::isIsread));
    }
}
