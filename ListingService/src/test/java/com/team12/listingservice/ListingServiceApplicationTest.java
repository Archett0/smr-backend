package com.team12.listingservice;

import com.team12.listingservice.model.Property;
import com.team12.listingservice.reponsitory.PropertyRepository;
import com.team12.listingservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListingServiceApplicationTest {

    private PropertyRepository propertyRepository;
    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        propertyService = new PropertyService(propertyRepository);
    }

    private Property createSampleProperty(Long id) {
        Property property = new Property();
        property.setId(id);
        property.setTitle("Sample Title");
        property.setDescription("Sample Description");
        property.setPrice(new BigDecimal("123456.78"));
        property.setAddress("123 Sample Street");
        property.setImg("sample.jpg");
//        property.setLocation(new GeoLocation(1.3521, 103.8198));
        property.setNumBedrooms(3);
        property.setNumBathrooms(2);
        property.setAvailable(true);
        property.setPostedAt(LocalDateTime.now());
        property.setAgentId("agent-123");
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
    void testGetAllProperties() {
        List<Property> mockList = Arrays.asList(
                createSampleProperty(1L),
                createSampleProperty(2L)
        );

        when(propertyRepository.findAll()).thenReturn(mockList);

        List<Property> result = propertyService.getAllProperties();
        assertEquals(2, result.size());
    }

    @Test
    void testGetPropertyById() {
        Property property = createSampleProperty(1L);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        Property result = propertyService.getPropertyById(1L).orElse(null);
        assertNotNull(result);
        assertEquals("Sample Title", result.getTitle());
    }

    @Test
    void testUpdateProperty() {
        Property existing = createSampleProperty(1L);
        Property update = createSampleProperty(1L);
        update.setTitle("Updated Title");
        update.setPrice(new BigDecimal("888888.88"));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(propertyRepository.save(any(Property.class))).thenReturn(update);

        Property result = propertyService.updateProperty(1L, update);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(new BigDecimal("888888.88"), result.getPrice());
    }

    @Test
    void testDeleteProperty() {
        doNothing().when(propertyRepository).deleteById(1L);
        propertyService.deleteProperty(1L);
        verify(propertyRepository, times(1)).deleteById(1L);
    }
}
