package com.team12.listingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.listingservice.config.RabbitMQConfig;
import com.team12.listingservice.model.Property;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send property sync message to SearchService
     */
    @Transactional
    public void syncPropertyToElasticsearch(String action, Property property) {
        try {
            log.info("Sending property sync message: action={}, propertyId={}", action, property.getId());
            
            Map<String, Object> message = createPropertySyncMessage(action, property);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROPERTY_SYNC_EXCHANGE,
                RabbitMQConfig.PROPERTY_SYNC_ROUTING_KEY,
                message
            );
            
            log.info("Successfully sent property sync message for property: {}", property.getId());
            
        } catch (Exception e) {
            log.error("Failed to send property sync message for property: {}", property.getId(), e);
            // In production, you might want to store failed messages for retry
            throw new RuntimeException("Failed to sync property data", e);
        }
    }

    /**
     * Send property deletion sync message
     */
    @Transactional
    public void syncPropertyDeletion(Long propertyId) {
        try {
            log.info("Sending property deletion sync message: propertyId={}", propertyId);
            
            Map<String, Object> propertyData = new HashMap<>();
            propertyData.put("id", String.valueOf(propertyId));
            
            Map<String, Object> message = Map.of(
                "action", "delete",
                "data", propertyData
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROPERTY_SYNC_EXCHANGE,
                RabbitMQConfig.PROPERTY_SYNC_ROUTING_KEY,
                message
            );
            
            log.info("Successfully sent property deletion sync message for property: {}", propertyId);
            
        } catch (Exception e) {
            log.error("Failed to send property deletion sync message for property: {}", propertyId, e);
            throw new RuntimeException("Failed to sync property deletion", e);
        }
    }

    /**
     * Create property sync message
     */
    private Map<String, Object> createPropertySyncMessage(String action, Property property) {
        Map<String, Object> propertyData = convertPropertyToMap(property);
        
        return Map.of(
            "action", action,
            "data", propertyData,
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Convert Property entity to Map for JSON serialization
     */
    private Map<String, Object> convertPropertyToMap(Property property) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", String.valueOf(property.getId()));
        data.put("title", property.getTitle());
        data.put("description", property.getDescription());
        data.put("price", property.getPrice());
        data.put("address", property.getAddress());
        data.put("img", property.getImg());
        data.put("numBedrooms", property.getNumBedrooms());
        data.put("numBathrooms", property.getNumBathrooms());
        data.put("available", property.isAvailable());
        data.put("agentId", property.getAgentId());
        data.put("postedAt", property.getPostedAt() != null ? property.getPostedAt().toString() : null);
        
        // Handle location data - skip for now since GeoLocation lacks getters
        // TODO: Add getters to GeoLocation class or implement location handling
        // if (property.getLocation() != null) {
        //     Map<String, Object> locationData = new HashMap<>();
        //     locationData.put("latitude", property.getLocation().getLatitude());
        //     locationData.put("longitude", property.getLocation().getLongitude());
        //     data.put("location", locationData);
        // }
        
        return data;
    }

    /**
     * Bulk sync all properties to Elasticsearch
     * Useful for initial data loading or recovery
     */
    public void bulkSyncProperties(java.util.List<Property> properties) {
        log.info("Starting bulk property sync for {} properties", properties.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Property property : properties) {
            try {
                syncPropertyToElasticsearch("create", property);
                successCount++;
                
                // Add small delay to avoid overwhelming the message queue
                Thread.sleep(50);
                
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to sync property {} during bulk operation", property.getId(), e);
            }
        }
        
        log.info("Bulk property sync completed: success={}, failures={}", successCount, failureCount);
    }
}