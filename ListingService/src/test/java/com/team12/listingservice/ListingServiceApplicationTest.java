package com.team12.listingservice;

import com.team12.clients.notification.NotificationClient;
import com.team12.clients.user.UserClient;
import com.team12.clients.userAction.UserActionClient;
import com.team12.listingservice.model.Property;
import com.team12.listingservice.model.PropertyDto;
import com.team12.listingservice.reponsitory.PropertyRepository;
import com.team12.listingservice.service.DataSyncService;
import com.team12.listingservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListingServiceApplicationTest {

    private PropertyRepository propertyRepository;
    private NotificationClient notificationClient;
    private PropertyService propertyService;
    private UserActionClient userActionClient;
    private UserClient userClient;
    private DataSyncService dataSyncService;

    @BeforeEach
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        notificationClient = mock(NotificationClient.class);
        userActionClient = mock(UserActionClient.class);
        userClient = mock(UserClient.class);
        dataSyncService = mock(DataSyncService.class);

        propertyService = new PropertyService(
                propertyRepository,
                notificationClient,
                userActionClient,
                userClient,
                dataSyncService
        );
    }

    private Property createSampleProperty(Long id) {
        Property property = new Property();
        property.setId(id);
        property.setTitle("Sample Title");
        property.setDescription("Sample Description");
        property.setPrice(new BigDecimal("123456.78"));
        property.setAddress("123 Sample Street");
        property.setImg("sample.jpg");
        property.setNumBedrooms(3);
        property.setNumBathrooms(2);
        property.setAvailable(true);
        property.setPostedAt(LocalDateTime.now());
        property.setAgentId("123");
        return property;
    }

    @Test
    void testAddProperty() {
        Property property = createSampleProperty(null);
        Property savedProperty = createSampleProperty(1L);

        when(propertyRepository.save(property)).thenReturn(savedProperty);

        Property result = propertyService.addProperty(property);
        assertNotNull(result);
        assertEquals("Sample Title", result.getTitle());
        assertEquals(3, result.getNumBedrooms());
    }

    @Test
    void testGetPropertyById() {
        Property property = createSampleProperty(1L);
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(userClient.getAgentInfoById(123L)).thenReturn(ResponseEntity.ok(List.of("AgentName", "12345678")));

        Optional<PropertyDto> result = propertyService.getPropertyById(1L);

        assertTrue(result.isPresent());
        assertEquals("Sample Title", result.get().getProperty().getTitle());
        assertEquals("AgentName", result.get().getUsername());
    }

    @Test
    void testGetAllPropertiesWithAgentInfo() {
        Property p = createSampleProperty(1L);
        when(propertyRepository.findAll()).thenReturn(List.of(p));
        when(userClient.getAgentInfoById(123L)).thenReturn(ResponseEntity.ok(List.of("AgentName", "12345678")));

        List<PropertyDto> result = propertyService.getAllPropertiesWithAgentInfo();
        assertEquals(1, result.size());
        assertEquals("AgentName", result.get(0).getUsername());
    }

    @Test
    void testCreateProperty() {
        Property property = createSampleProperty(null);
        Property saved = createSampleProperty(1L);

        when(propertyRepository.save(property)).thenReturn(saved);

        Property result = propertyService.createProperty(property);
        assertNotNull(result);
        verify(dataSyncService).syncPropertyToElasticsearch("create", saved);
    }

    @Test
    void testUpdateProperty() {
        Property existing = createSampleProperty(1L);
        Property update = createSampleProperty(1L);
        update.setTitle("Updated Title");
        update.setPrice(new BigDecimal("888888.88"));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(propertyRepository.save(any(Property.class))).thenReturn(update);
        when(userActionClient.getPriceAlertUsers(1L)).thenReturn(List.of(1L, 2L));

        Property result = propertyService.updateProperty(1L, update);
        assertEquals("Updated Title", result.getTitle());

        verify(notificationClient, times(2)).sendNotification(any());
        verify(dataSyncService).syncPropertyToElasticsearch(eq("update"), any(Property.class));
    }

    @Test
    void testDeleteProperty() {
        when(propertyRepository.existsById(1L)).thenReturn(true);

        propertyService.deleteProperty(1L);

        verify(propertyRepository).deleteById(1L);
        verify(dataSyncService).syncPropertyDeletion(1L);
    }

    @Test
    void testBulkSyncAllProperties() {
        List<Property> properties = List.of(createSampleProperty(1L));
        when(propertyRepository.findAll()).thenReturn(properties);

        propertyService.bulkSyncAllProperties();

        verify(dataSyncService).bulkSyncProperties(properties);
    }

    @Test
    void testGetPropertyStatistics() {
        when(propertyRepository.count()).thenReturn(100L);
        when(propertyRepository.countByAvailableTrue()).thenReturn(80L);

        Map<String, Object> stats = propertyService.getPropertyStatistics();

        assertEquals(100L, stats.get("totalProperties"));
        assertEquals(80L, stats.get("availableProperties"));
        assertEquals(20L, stats.get("unavailableProperties"));
    }
}
