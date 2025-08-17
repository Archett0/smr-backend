package com.team12.searchservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.document.UserDocument;
import com.team12.searchservice.repository.PropertySearchRepository;
import com.team12.searchservice.repository.UserSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSyncServiceTest {

    @Mock
    private PropertySearchRepository propertySearchRepository;

    @Mock
    private UserSearchRepository userSearchRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataSyncService dataSyncService;

    private Map<String, Object> samplePropertyData;
    private Map<String, Object> sampleUserData;
    private Map<String, Object> propertyMessage;
    private Map<String, Object> userMessage;

    @BeforeEach
    void setUp() {
        // Sample property data
        samplePropertyData = new HashMap<>();
        samplePropertyData.put("id", "1");
        samplePropertyData.put("title", "Modern Apartment");
        samplePropertyData.put("description", "Beautiful modern apartment");
        samplePropertyData.put("address", "123 Main St, Singapore");
        samplePropertyData.put("price", new BigDecimal("3500.0"));
        samplePropertyData.put("numBedrooms", 2);
        samplePropertyData.put("numBathrooms", 2);
        samplePropertyData.put("city", "Singapore");
        samplePropertyData.put("district", "Central");
        samplePropertyData.put("propertyType", "Apartment");
        samplePropertyData.put("available", true);
        samplePropertyData.put("agentId", "agent1");
        samplePropertyData.put("img", "image.jpg");

        // Sample user data
        sampleUserData = new HashMap<>();
        sampleUserData.put("id", "user1");
        sampleUserData.put("email", "user@example.com");
        sampleUserData.put("name", "John Doe");
        sampleUserData.put("role", "TENANT");
        sampleUserData.put("verified", true);

        // Sample messages
        propertyMessage = new HashMap<>();
        propertyMessage.put("action", "create");
        propertyMessage.put("data", samplePropertyData);

        userMessage = new HashMap<>();
        userMessage.put("action", "create");
        userMessage.put("data", sampleUserData);
    }

    @Test
    void handlePropertySync_ShouldProcessCreateAction() {
        // Given
        when(propertySearchRepository.save(any(PropertyDocument.class))).thenReturn(null);

        // When
        dataSyncService.handlePropertySync(propertyMessage);

        // Then
        verify(propertySearchRepository).save(any(PropertyDocument.class));
    }

    @Test
    void handlePropertySync_ShouldProcessUpdateAction() {
        // Given
        propertyMessage.put("action", "update");
        when(propertySearchRepository.save(any(PropertyDocument.class))).thenReturn(null);

        // When
        dataSyncService.handlePropertySync(propertyMessage);

        // Then
        verify(propertySearchRepository).save(any(PropertyDocument.class));
    }

    @Test
    void handlePropertySync_ShouldProcessDeleteAction() {
        // Given
        propertyMessage.put("action", "delete");
        doNothing().when(propertySearchRepository).deleteById(anyString());

        // When
        dataSyncService.handlePropertySync(propertyMessage);

        // Then
        verify(propertySearchRepository).deleteById("1");
    }

    @Test
    void handlePropertySync_ShouldHandleStringMessage() throws JsonProcessingException {
        // Given
        String jsonMessage = "{\"action\":\"create\",\"data\":{\"id\":\"1\"}}";
        when(objectMapper.readValue(eq(jsonMessage), eq(Map.class))).thenReturn(propertyMessage);
        when(propertySearchRepository.save(any(PropertyDocument.class))).thenReturn(null);

        // When
        dataSyncService.handlePropertySync(jsonMessage);

        // Then
        verify(objectMapper).readValue(eq(jsonMessage), eq(Map.class));
        verify(propertySearchRepository).save(any(PropertyDocument.class));
    }

    @Test
    void handlePropertySync_ShouldHandleMessageObject() throws JsonProcessingException {
        // Given
        Message message = mock(Message.class);
        String messageBody = "{\"action\":\"create\",\"data\":{\"id\":\"1\"}}";
        when(message.getBody()).thenReturn(messageBody.getBytes());
        when(objectMapper.readValue(eq(messageBody), eq(Map.class))).thenReturn(propertyMessage);
        when(propertySearchRepository.save(any(PropertyDocument.class))).thenReturn(null);

        // When
        dataSyncService.handlePropertySync(message);

        // Then
        verify(objectMapper).readValue(eq(messageBody), eq(Map.class));
        verify(propertySearchRepository).save(any(PropertyDocument.class));
    }

    @Test
    void handlePropertySync_ShouldHandleUnknownAction() {
        // Given
        propertyMessage.put("action", "unknown");

        // When
        dataSyncService.handlePropertySync(propertyMessage);

        // Then
        verify(propertySearchRepository, never()).save(any(PropertyDocument.class));
        verify(propertySearchRepository, never()).deleteById(anyString());
    }

    @Test
    void handlePropertySync_ShouldHandleInvalidMessageType() {
        // Given
        Integer invalidMessage = 123;

        // When
        dataSyncService.handlePropertySync(invalidMessage);

        // Then
        verify(propertySearchRepository, never()).save(any(PropertyDocument.class));
        verify(propertySearchRepository, never()).deleteById(anyString());
    }

    @Test
    void handleUserSync_ShouldProcessCreateAction() {
        // Given
        when(userSearchRepository.save(any(UserDocument.class))).thenReturn(null);

        // When
        dataSyncService.handleUserSync(userMessage);

        // Then
        verify(userSearchRepository).save(any(UserDocument.class));
    }

    @Test
    void handleUserSync_ShouldProcessDeleteAction() {
        // Given
        userMessage.put("action", "delete");
        doNothing().when(userSearchRepository).deleteById(anyString());

        // When
        dataSyncService.handleUserSync(userMessage);

        // Then
        verify(userSearchRepository).deleteById("user1");
    }

    @Test
    void handleUserSync_ShouldHandleUnknownAction() {
        // Given
        userMessage.put("action", "unknown");

        // When
        dataSyncService.handleUserSync(userMessage);

        // Then
        verify(userSearchRepository, never()).save(any(UserDocument.class));
        verify(userSearchRepository, never()).deleteById(anyString());
    }
}