package com.team12.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.clients.notification.dto.NotificationRequest;
import com.team12.notificationservice.controller.NotificationController;
import com.team12.notificationservice.dto.NotificationCreateDto;
import com.team12.notificationservice.model.Notification;
import com.team12.notificationservice.model.NotificationType;
import com.team12.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationServiceApplicationTest {

    private MockMvc mockMvc;
    private NotificationService notificationService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Notification sample(Long id) {
        return Notification.builder()
                .id(id)
                .fromId("4")
                .fromDeviceId("fd")
                .toId("3")
                .toDeviceId("td")
                .message("hi")
                .type(NotificationType.SYSTEM)
                .isread(false)
                .build();
    }

    @Test
    void create_shouldReturnSavedNotification() throws Exception {
        NotificationCreateDto dto = NotificationCreateDto.builder()
                .fromId("4").toId("3").message("hello").type(NotificationType.SYSTEM).build();

        when(notificationService.createNotification(any(NotificationCreateDto.class)))
                .thenReturn(sample(1L));

        mockMvc.perform(post("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(notificationService).createNotification(any(NotificationCreateDto.class));
    }

    @Test
    void send_shouldReturn200() throws Exception {
        NotificationRequest req = new NotificationRequest("3", "system hi", com.team12.clients.notification.dto.NotificationType.SYSTEM);

        mockMvc.perform(post("/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(notificationService).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(sample(1L), sample(2L)));

        mockMvc.perform(get("/notification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getById_shouldReturnOne() throws Exception {
        when(notificationService.getNotificationById(10L)).thenReturn(Optional.of(sample(10L)));

        mockMvc.perform(get("/notification/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    void getByTo_shouldReturnList() throws Exception {
        when(notificationService.getByToId("3")).thenReturn(List.of(sample(1L)));

        mockMvc.perform(get("/notification/to/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void update_shouldReturnSavedEntity() throws Exception {
        Notification n = sample(5L);
        when(notificationService.updateNotification(any(Notification.class))).thenReturn(n);

        mockMvc.perform(put("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(n)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/notification/7"))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(7L);
    }

    @Test
    void acceptDeny_shouldReturn200() throws Exception {
        when(notificationService.acceptNotification(1L)).thenReturn(sample(100L));
        when(notificationService.denyNotification(2L)).thenReturn(sample(200L));

        mockMvc.perform(post("/notification/acceptApp/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)));

        mockMvc.perform(post("/notification/denyApp/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(200)));
    }
}
