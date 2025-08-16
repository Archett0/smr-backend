package com.team12.notificationservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team12.notificationservice.config.RabbitMQListener;
import com.team12.notificationservice.dto.NotificationDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.model.NotificationType;
import com.team12.notificationservice.service.MessagingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQListenerTest {

    private MessagingService messagingService;
    private RabbitMQListener listener;

    @BeforeEach
    void setUp() {
        messagingService = mock(MessagingService.class);
        listener = new RabbitMQListener(messagingService);
    }

    private Notification sample() {
        Notification n = new Notification();
        n.setId(1L);
        n.setFromId("2L");
        n.setFromDeviceId("fromDevice");
        n.setToId("3L");
        n.setToDeviceId("toDevice");
        n.setMessage("hello");
        n.setType(NotificationType.SYSTEM);
        n.setIsread(false);
        n.setCreatedAt(java.time.LocalDateTime.now());
        return n;
    }

    @Test
    void receiveMessage_ok_callsMessagingServiceWithExpectedArgs() throws Exception {
        Notification n = sample();

        listener.receiveMessage(n);

        ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> title = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificationDto> dto = ArgumentCaptor.forClass(NotificationDto.class);

        verify(messagingService, times(1))
                .sendNotificationToDevice(to.capture(), title.capture(), body.capture(), dto.capture());

        assertEquals("toDevice", to.getValue());
        assertEquals(n.getType().toString(), title.getValue());
        assertEquals("hello", body.getValue());
        assertEquals(n.getToId(), dto.getValue().getToId());
        assertEquals(n.getMessage(), dto.getValue().getMessage());
    }

    @Test
    void receiveMessage_jsonException_branchCovered() throws Exception {
        Notification n = sample();
        doThrow(new JsonProcessingException("boom"){ })
                .when(messagingService)
                .sendNotificationToDevice(anyString(), anyString(), anyString(), any());

        assertDoesNotThrow(() -> listener.receiveMessage(n));
        verify(messagingService, times(1))
                .sendNotificationToDevice(anyString(), anyString(), anyString(), any());
    }

    @Test
    void receiveMessage_interruptedException_setsInterruptFlag() throws Exception {
        Notification n = sample();
        Thread.interrupted();

        doThrow(new InterruptedException("interrupted"))
                .when(messagingService)
                .sendNotificationToDevice(anyString(), anyString(), anyString(), any());

        listener.receiveMessage(n);

        assertTrue(Thread.currentThread().isInterrupted(), "");
        Thread.interrupted();
    }

    @Test
    void receiveMessage_executionException_branchCovered() throws Exception {
        Notification n = sample();

        doThrow(new ExecutionException("exec", new RuntimeException()))
                .when(messagingService)
                .sendNotificationToDevice(anyString(), anyString(), anyString(), any());

        assertDoesNotThrow(() -> listener.receiveMessage(n));
    }
}
