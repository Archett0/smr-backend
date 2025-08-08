package com.team12.listingservice.service;

import com.team12.clients.notification.NotificationClient;
import com.team12.clients.notification.dto.NotificationRequest;
import com.team12.clients.notification.dto.NotificationType;
import com.team12.clients.user.UserClient;
import com.team12.clients.userAction.UserActionClient;
import com.team12.listingservice.model.Property;
import com.team12.listingservice.model.PropertyDto;
import com.team12.listingservice.reponsitory.PropertyRepository;
import com.team12.listingservice.service.DataSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.team12.listingservice.exception.PropertyExceptions.*;

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
        if (property != null) {
            Long agentId = Long.valueOf(property.getAgentId());
            List<String> agentInfo = userClient.getAgentInfoById(agentId).getBody();
            PropertyDto dto = new PropertyDto();
            dto.setProperty(property);
            dto.setUsername(agentInfo.get(0));
            dto.setPhoneNumber(agentInfo.get(1));
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    @Transactional
    public Property createProperty(Property property) {
        try {
            log.info("Creating new property: {}", property.getTitle());
            property.setPostedAt(LocalDateTime.now());
            Property savedProperty = propertyRepository.save(property);
            log.info("Property saved to database with ID: {}", savedProperty.getId());
            dataSyncService.syncPropertyToElasticsearch("create", savedProperty);
            return savedProperty;
        } catch (Exception e) {
            log.error("Error creating property: {}", property.getTitle(), e);
            throw new PropertyCreateException("Failed to create property", e);
        }
    }

    @Transactional
    public Property updateProperty(Long id, Property property) {
        try {
            log.info("Updating property with ID: {}", id);

            return propertyRepository.findById(id).map(existing -> {
                BigDecimal oldPrice = existing.getPrice();
                BigDecimal newPrice = property.getPrice();

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

                Property updatedProperty = propertyRepository.save(existing);
                log.info("Property updated in database: {}", updatedProperty.getId());

                if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
                    List<Long> userId = userActionClient.getPriceAlertUsers(id);
                    for (Long uid : userId) {
                        NotificationRequest notificationRequest = new NotificationRequest(
                                uid.toString(),
                                "Property " + existing.getTitle() +
                                        " price changed from " + oldPrice +
                                        " to " + newPrice,
                                NotificationType.SYSTEM
                        );
                        notificationClient.sendNotification(notificationRequest);
                    }
                }

                dataSyncService.syncPropertyToElasticsearch("update", updatedProperty);

                return updatedProperty;

            }).orElseThrow(() -> {
                log.warn("Property not found for update: {}", id);
                return new PropertyNotFoundException(id);
            });

        } catch (Exception e) {
            log.error("Error updating property: {}", id, e);
            throw new PropertyCreateException("Failed to update property", e);
        }
    }

    @Transactional
    public void deleteProperty(Long id) {
        try {
            log.info("Deleting property with ID: {}", id);

            if (!propertyRepository.existsById(id)) {
                log.warn("Property not found for deletion: {}", id);
                throw new PropertyNotFoundException(id);
            }

            propertyRepository.deleteById(id);
            log.info("Property deleted from database: {}", id);

            dataSyncService.syncPropertyDeletion(id);
        } catch (Exception e) {
            log.error("Error deleting property: {}", id, e);
            throw new PropertyDeleteException("Failed to delete property", e);
        }
    }

    public Property addProperty(Property property) {
        return property;
    }

    public void bulkSyncAllProperties() {
        log.info("Starting bulk sync of all properties to Elasticsearch");

        List<Property> allProperties = propertyRepository.findAll();
        log.info("Found {} properties to sync", allProperties.size());

        dataSyncService.bulkSyncProperties(allProperties);

        log.info("Bulk sync completed");
    }

    public Map<String, Object> getPropertyStatistics() {
        long total = propertyRepository.count();
        long available = propertyRepository.countByAvailableTrue();

        return Map.of(
                "totalProperties", total,
                "availableProperties", available,
                "unavailableProperties", total - available
        );
    }
}
