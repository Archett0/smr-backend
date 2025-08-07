package com.team12.listingservice.service;

import com.team12.clients.notification.NotificationClient;
import com.team12.clients.notification.dto.NotificationRequest;
import com.team12.clients.notification.dto.NotificationType;
import com.team12.clients.user.UserClient;
import com.team12.clients.userAction.UserActionClient;
import com.team12.listingservice.model.Property;
import com.team12.listingservice.model.PropertyDto;
import com.team12.listingservice.reponsitory.PropertyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final NotificationClient notificationClient;
    private final UserActionClient userActionClient;
    private final UserClient userClient;
    private final DataSyncService dataSyncService;

    public List<PropertyDto> getAllPropertiesWithAgentInfo() {
        List<Property> all = propertyRepository.findAll();
        List<PropertyDto> dtos = new ArrayList<>(all.size());

        for (Property p : all) {
            Long agentId = Long.valueOf(p.getAgentId());
            List<String> agentInfo = userClient.getAgentInfoById(agentId).getBody();
            PropertyDto dto = new PropertyDto();
            dto.setProperty(p);
            dto.setUsername(agentInfo.get(0));
            dto.setPhoneNumber(agentInfo.get(1));

            dtos.add(dto);
        }

        return dtos;
    }

    public Optional<PropertyDto> getPropertyById(Long id) {
        Property property = propertyRepository.findById(id).orElse(null);
        if(property != null) {
            Long agentId = Long.valueOf(property.getAgentId());
            List<String> agentInfo = userClient.getAgentInfoById(agentId).getBody();
            PropertyDto dto = new PropertyDto();
            dto.setProperty(property);
            dto.setUsername(agentInfo.get(0));
            dto.setPhoneNumber(agentInfo.get(1));
            return Optional.of(dto);
        }
        return null;
    }

    @Transactional
    public Property createProperty(Property property) {
        try {
            log.info("Creating new property: {}", property.getTitle());
            
            // Set creation timestamp
            property.setPostedAt(LocalDateTime.now());
            
            // Save to database
            Property savedProperty = propertyRepository.save(property);
            log.info("Property saved to database with ID: {}", savedProperty.getId());
            
            // Sync to Elasticsearch
            dataSyncService.syncPropertyToElasticsearch("create", savedProperty);
            
            return savedProperty;
            
        } catch (Exception e) {
            log.error("Error creating property: {}", property.getTitle(), e);
            throw new RuntimeException("Failed to create property", e);
        }
    }

    @Transactional
    public Property updateProperty(Long id, Property property) {
        try {
            log.info("Updating property with ID: {}", id);
            
            return propertyRepository.findById(id).map(existing -> {
                // Store old price for comparison
                BigDecimal oldPrice = existing.getPrice();
                BigDecimal newPrice = property.getPrice();
                
                // Update all fields
                existing.setTitle(property.getTitle());
                existing.setDescription(property.getDescription());
                existing.setPrice(newPrice);
                existing.setAddress(property.getAddress());
                existing.setImg(property.getImg());
                existing.setLocation(property.getLocation());
                existing.setNumBedrooms(property.getNumBedrooms());
                existing.setNumBathrooms(property.getNumBathrooms());
                existing.setAvailable(property.isAvailable());
                existing.setAgentId(property.getAgentId());
                
                // Save to database
                Property updatedProperty = propertyRepository.save(existing);
                log.info("Property updated in database: {}", updatedProperty.getId());
                
                // Send price change notifications if price changed
                if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
                    List<Long> userId = userActionClient.getPriceAlertUsers(id);
                    for(int i = 0; i < userId.size(); i++) {
                        NotificationRequest notificationRequest = new NotificationRequest(
                                userId.get(i).toString(),
                                "Property " + existing.getTitle() +
                                        " price changed from " + oldPrice +
                                        " to " + newPrice,
                                NotificationType.SYSTEM
                        );
                        notificationClient.sendNotification(notificationRequest);
                    }
                }
                
                // Sync to Elasticsearch
                dataSyncService.syncPropertyToElasticsearch("update", updatedProperty);
                
                return updatedProperty;
                
            }).orElseThrow(() -> {
                log.warn("Property not found for update: {}", id);
                return new RuntimeException("Property not found: " + id);
            });
            
        } catch (Exception e) {
            log.error("Error updating property: {}", id, e);
            throw new RuntimeException("Failed to update property", e);
        }
    }

    @Transactional
    public void deleteProperty(Long id) {
        try {
            log.info("Deleting property with ID: {}", id);
            
            // Check if property exists
            if (!propertyRepository.existsById(id)) {
                log.warn("Property not found for deletion: {}", id);
                throw new RuntimeException("Property not found: " + id);
            }
            
            // Delete from database
            propertyRepository.deleteById(id);
            log.info("Property deleted from database: {}", id);
            
            // Sync deletion to Elasticsearch
            dataSyncService.syncPropertyDeletion(id);
            
        } catch (Exception e) {
            log.error("Error deleting property: {}", id, e);
            throw new RuntimeException("Failed to delete property", e);
        }
    }

    public Property addProperty(Property property) {
        return property;
    }

    /**
     * Bulk sync all properties to Elasticsearch
     * Useful for initial setup or recovery
     */
    public void bulkSyncAllProperties() {
        log.info("Starting bulk sync of all properties to Elasticsearch");
        
        List<Property> allProperties = propertyRepository.findAll();
        log.info("Found {} properties to sync", allProperties.size());
        
        dataSyncService.bulkSyncProperties(allProperties);
        
        log.info("Bulk sync completed");
    }

    /**
     * Get property statistics
     */
    public Map<String, Object> getPropertyStatistics() {
        long totalProperties = propertyRepository.count();
        long availableProperties = propertyRepository.countByAvailableTrue();
        
        return Map.of(
            "totalProperties", totalProperties,
            "availableProperties", availableProperties,
            "unavailableProperties", totalProperties - availableProperties
        );
    }
}
