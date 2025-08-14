package com.team12.notificationservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFutures;
import com.google.api.core.SettableApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.team12.notificationservice.service.MessagingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import com.google.api.core.ApiFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagingServiceTest {

    @Test
    @DisplayName("sendNotificationToDevice: Normal transmission")
    void send_ok() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MessagingService service = new MessagingService(mapper);

        FirebaseMessaging fm = mock(FirebaseMessaging.class);
        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(fm);

            when(fm.sendAsync(any(Message.class)))
                    .thenReturn(ApiFutures.immediateFuture("mock-id"));

            var dto = mock(com.team12.notificationservice.dto.NotificationDto.class);

            service.sendNotificationToDevice("dev-token", "Title", "Body", dto);

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(fm).sendAsync(captor.capture());
            assertNotNull(captor.getValue());
        }
    }

    @Test
    @DisplayName("sendNotificationToDevice: future.get() exception InterruptedException")
    void send_interrupted_exception() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MessagingService service = new MessagingService(mapper);

        FirebaseMessaging fm = mock(FirebaseMessaging.class);
        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(fm);

            @SuppressWarnings("unchecked")
            ApiFuture<String> future = mock(ApiFuture.class);
            when(future.get()).thenThrow(new InterruptedException("interrupted"));
            when(fm.sendAsync(any(Message.class))).thenReturn(future);

            assertThrows(InterruptedException.class, () ->
                    service.sendNotificationToDevice("dev-token", "T", "B",
                            mock(com.team12.notificationservice.dto.NotificationDto.class))
            );
        }
    }

    @Test
    @DisplayName("sendNotificationToDevice: sendAsync.get() exception ExecutionException")
    void send_exec_exception() {
        ObjectMapper mapper = new ObjectMapper();
        MessagingService service = new MessagingService(mapper);

        FirebaseMessaging fm = mock(FirebaseMessaging.class);
        try (MockedStatic<FirebaseMessaging> mocked = mockStatic(FirebaseMessaging.class)) {
            mocked.when(FirebaseMessaging::getInstance).thenReturn(fm);

            SettableApiFuture<String> failed = SettableApiFuture.create();
            failed.setException(new RuntimeException("upstream"));
            when(fm.sendAsync(any(Message.class))).thenReturn(failed);

            assertThrows(ExecutionException.class, () ->
                    service.sendNotificationToDevice(
                            "dev-token", "T", "B",
                            mock(com.team12.notificationservice.dto.NotificationDto.class)
                    )
            );
        }
    }

    @Test
    @DisplayName("sendNotificationToDevice: ObjectMapper exception JsonProcessingException")
    void send_json_exception() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("boom") {});
        MessagingService service = new MessagingService(mapper);

        assertThrows(JsonProcessingException.class, () ->
                service.sendNotificationToDevice(
                        "dev-token", "T", "B",
                        mock(com.team12.notificationservice.dto.NotificationDto.class)
                )
        );
    }
}
