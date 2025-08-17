package com.team12.searchservice.controller;

import com.team12.searchservice.service.DataSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private DataSyncService dataSyncService;

    @InjectMocks
    private AdminController adminController;

    private Map<String, Object> mockStats;

    @BeforeEach
    void setUp() {
        mockStats = new HashMap<>();
        mockStats.put("totalProperties", 100L);
        mockStats.put("totalUsers", 50L);
        mockStats.put("lastSync", System.currentTimeMillis());
        mockStats.put("syncStatus", "active");
    }

    @Test
    void getSyncStatistics_ShouldReturnStats_WhenServiceSucceeds() {
        // Given
        when(dataSyncService.getSyncStatistics()).thenReturn(mockStats);

        // When
        ResponseEntity<Map<String, Object>> response = adminController.getSyncStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalProperties")).isEqualTo(100L);
        assertThat(response.getBody().get("totalUsers")).isEqualTo(50L);
        assertThat(response.getBody().get("syncStatus")).isEqualTo("active");
        
        verify(dataSyncService).getSyncStatistics();
    }

    @Test
    void getSyncStatistics_ShouldReturnError_WhenServiceThrowsException() {
        // Given
        String errorMessage = "Database connection failed";
        when(dataSyncService.getSyncStatistics()).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<Map<String, Object>> response = adminController.getSyncStatistics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to get statistics: " + errorMessage);
        
        verify(dataSyncService).getSyncStatistics();
    }

    @Test
    void bulkIndexProperties_ShouldReturnSuccess_WhenServiceSucceeds() {
        // Given
        doNothing().when(dataSyncService).bulkIndexProperties();

        // When
        ResponseEntity<Map<String, String>> response = adminController.bulkIndexProperties();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("message")).isEqualTo("Bulk property indexing initiated");
        
        verify(dataSyncService).bulkIndexProperties();
    }

    @Test
    void bulkIndexProperties_ShouldReturnError_WhenServiceThrowsException() {
        // Given
        String errorMessage = "Elasticsearch unavailable";
        doThrow(new RuntimeException(errorMessage)).when(dataSyncService).bulkIndexProperties();

        // When
        ResponseEntity<Map<String, String>> response = adminController.bulkIndexProperties();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("Bulk indexing failed: " + errorMessage);
        
        verify(dataSyncService).bulkIndexProperties();
    }

    @Test
    void bulkIndexUsers_ShouldReturnSuccess_WhenServiceSucceeds() {
        // Given
        doNothing().when(dataSyncService).bulkIndexUsers();

        // When
        ResponseEntity<Map<String, String>> response = adminController.bulkIndexUsers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("message")).isEqualTo("Bulk user indexing initiated");
        
        verify(dataSyncService).bulkIndexUsers();
    }

    @Test
    void bulkIndexUsers_ShouldReturnError_WhenServiceThrowsException() {
        // Given
        String errorMessage = "Index creation failed";
        doThrow(new RuntimeException(errorMessage)).when(dataSyncService).bulkIndexUsers();

        // When
        ResponseEntity<Map<String, String>> response = adminController.bulkIndexUsers();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("Bulk indexing failed: " + errorMessage);
        
        verify(dataSyncService).bulkIndexUsers();
    }

    @Test
    void healthCheck_ShouldReturnHealthyStatus() {
        // When
        ResponseEntity<Map<String, Object>> response = adminController.healthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("healthy");
        assertThat(response.getBody().get("service")).isEqualTo("SearchService");
        assertThat(response.getBody().get("elasticsearch")).isEqualTo("connected");
        assertThat(response.getBody().get("rabbitmq")).isEqualTo("connected");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        
        verifyNoInteractions(dataSyncService);
    }
}