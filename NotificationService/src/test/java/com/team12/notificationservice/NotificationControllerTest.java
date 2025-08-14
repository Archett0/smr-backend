package com.team12.notificationservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.notificationservice.controller.NotificationController;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.model.NotificationType;
import com.team12.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NotificationService notificationService;

    private Notification sample(Long id) {
        return Notification.builder()
                .id(id)
                .fromId("1")
                .fromDeviceId("dev-from")
                .toId("2")
                .toDeviceId("dev-to")
                .message("hello")
                .type(NotificationType.SYSTEM)
                .isread(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /notification -> 200 OK")
    void create_ok() throws Exception {
        Notification saved = sample(100L);
        Mockito.when(notificationService.createNotification(any())).thenReturn(saved);

        String body = """
        {
          "fromId":"1",
          "toId":"2",
          "message":"hello",
          "type":"SYSTEM"
        }
        """;

        mvc.perform(post("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.type", is("SYSTEM")));
    }

    @Test
    @DisplayName("POST /notification/send -> 200 OK (void)")
    void send_ok() throws Exception {
        // 只需验证 200，无返回体
        String body = """
        {
          "toUserId":"2",
          "message":"system msg"
        }
        """;
        mvc.perform(post("/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        Mockito.verify(notificationService).sendNotification(any());
    }

    @Test
    @DisplayName("GET /notification -> 200 OK")
    void getAll_ok() throws Exception {
        Mockito.when(notificationService.getAllNotifications()).thenReturn(List.of(sample(1L), sample(2L)));

        mvc.perform(get("/notification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /notification/{id} -> 200 OK / 404 Not Found")
    void getById_ok_and_404() throws Exception {
        Mockito.when(notificationService.getNotificationById(1L)).thenReturn(Optional.of(sample(1L)));
        Mockito.when(notificationService.getNotificationById(9L)).thenReturn(Optional.empty());

        mvc.perform(get("/notification/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        mvc.perform(get("/notification/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /notification/from/{fromId} & /to/{toId} -> 200 OK")
    void getByFrom_to_ok() throws Exception {
        Mockito.when(notificationService.getByFromId("1")).thenReturn(List.of(sample(1L)));
        Mockito.when(notificationService.getByToId("2")).thenReturn(List.of(sample(2L)));

        mvc.perform(get("/notification/from/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromId", is("1")));

        mvc.perform(get("/notification/to/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toId", is("2")));
    }

    @Test
    @DisplayName("PUT /notification -> 200 OK")
    void update_ok() throws Exception {
        Notification n = sample(123L);
        Mockito.when(notificationService.updateNotification(any())).thenReturn(n);

        String body = objectMapper.writeValueAsString(n);
        mvc.perform(put("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(123)));
    }

    @Test
    @DisplayName("DELETE /notification/{id} -> 204 No Content")
    void delete_noContent() throws Exception {
        mvc.perform(delete("/notification/55"))
                .andExpect(status().isNoContent());
        Mockito.verify(notificationService).deleteNotification(55L);
    }

    @Test
    @DisplayName("POST /acceptApp/{id} -> 200 OK / 500")
    void accept_ok_and_500() throws Exception {
        Mockito.when(notificationService.acceptNotification(5L)).thenReturn(sample(999L));
        Mockito.when(notificationService.acceptNotification(6L)).thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/notification/acceptApp/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(999)));

        mvc.perform(post("/notification/acceptApp/6"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /denyApp/{id} -> 200 OK / 500")
    void deny_ok_and_500() throws Exception {
        Mockito.when(notificationService.denyNotification(7L)).thenReturn(sample(1000L));
        Mockito.when(notificationService.denyNotification(8L)).thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/notification/denyApp/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1000)));

        mvc.perform(post("/notification/denyApp/8"))
                .andExpect(status().isInternalServerError());
    }
}
