package com.team12.searchservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.searchservice.config.RabbitMQConfig;
import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.document.UserDocument;
import com.team12.searchservice.repository.PropertySearchRepository;
import com.team12.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final PropertySearchRepository propertySearchRepository;
    private final UserSearchRepository userSearchRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Listen for property data synchronization messages
     */
    @RabbitListener(queues = RabbitMQConfig.PROPERTY_SYNC_QUEUE)
    public void handlePropertySync(Object message) {
        try {
            log.info("Received property sync message: {}", message);
            
            Map<String, Object> messageData;
            if (message instanceof String) {
                messageData = objectMapper.readValue((String) message, Map.class);
            } else if (message instanceof Map) {
                messageData = (Map<String, Object>) message;
            } else if (message instanceof Message) {
                String messageBody = new String(((Message) message).getBody());
                messageData = objectMapper.readValue(messageBody, Map.class);
            } else {
                log.error("Unexpected message type: {}", message.getClass());
                return;
            }
            String action = (String) messageData.get("action");
            Map<String, Object> propertyData = (Map<String, Object>) messageData.get("data");
            
            switch (action.toLowerCase()) {
                case "create":
                case "update":
                    syncProperty(propertyData);
                    break;
                case "delete":
                    deleteProperty((String) propertyData.get("id"));
                    break;
                default:
                    log.warn("Unknown property sync action: {}", action);
            }
            
        } catch (Exception e) {
            log.error("Error processing property sync message: {}", message, e);
        }
    }

    /**
     * Listen for user data synchronization messages
     */
    @RabbitListener(queues = RabbitMQConfig.USER_SYNC_QUEUE)
    public void handleUserSync(Object message) {
        try {
            log.info("Received user sync message: {}", message);
            
            Map<String, Object> messageData;
            if (message instanceof String) {
                messageData = objectMapper.readValue((String) message, Map.class);
            } else if (message instanceof Map) {
                messageData = (Map<String, Object>) message;
            } else if (message instanceof Message) {
                String messageBody = new String(((Message) message).getBody());
                messageData = objectMapper.readValue(messageBody, Map.class);
            } else {
                log.error("Unexpected message type: {}", message.getClass());
                return;
            }
            String action = (String) messageData.get("action");
            Map<String, Object> userData = (Map<String, Object>) messageData.get("data");
            
            switch (action.toLowerCase()) {
                case "create":
                case "update":
                    syncUser(userData);
                    break;
                case "delete":
                    deleteUser((String) userData.get("id"));
                    break;
                default:
                    log.warn("Unknown user sync action: {}", action);
            }
            
        } catch (Exception e) {
            log.error("Error processing user sync message: {}", message, e);
        }
    }

    /**
     * Synchronize property data to Elasticsearch
     */
    private void syncProperty(Map<String, Object> propertyData) {
        try {
            PropertyDocument propertyDocument = convertToPropertyDocument(propertyData);
            propertySearchRepository.save(propertyDocument);
            log.info("Successfully synced property: {}", propertyDocument.getId());
            
        } catch (Exception e) {
            log.error("Error syncing property data: {}", propertyData, e);
        }
    }

    /**
     * Delete property from Elasticsearch
     */
    private void deleteProperty(String propertyId) {
        try {
            propertySearchRepository.deleteById(propertyId);
            log.info("Successfully deleted property: {}", propertyId);
            
        } catch (Exception e) {
            log.error("Error deleting property: {}", propertyId, e);
        }
    }

    /**
     * Synchronize user data to Elasticsearch
     */
    private void syncUser(Map<String, Object> userData) {
        try {
            UserDocument userDocument = convertToUserDocument(userData);
            userSearchRepository.save(userDocument);
            log.info("Successfully synced user: {}", userDocument.getId());
            
        } catch (Exception e) {
            log.error("Error syncing user data: {}", userData, e);
        }
    }

    /**
     * Delete user from Elasticsearch
     */
    private void deleteUser(String userId) {
        try {
            userSearchRepository.deleteById(userId);
            log.info("Successfully deleted user: {}", userId);
            
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
        }
    }

    /**
     * Convert property data to PropertyDocument
     */
    private PropertyDocument convertToPropertyDocument(Map<String, Object> propertyData) {
        PropertyDocument.PropertyDocumentBuilder builder = PropertyDocument.builder()
                .id(String.valueOf(propertyData.get("id")))
                .title((String) propertyData.get("title"))
                .description((String) propertyData.get("description"))
                .address((String) propertyData.get("address"))
                .img((String) propertyData.get("img"))
                .agentId((String) propertyData.get("agentId"))
                .available((Boolean) propertyData.getOrDefault("available", true))
                .lastUpdated(LocalDateTime.now());

        // Handle price
        Object priceObj = propertyData.get("price");
        if (priceObj != null) {
            if (priceObj instanceof BigDecimal) {
                builder.price((BigDecimal) priceObj);
            } else if (priceObj instanceof Number) {
                builder.price(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            }
        }

        // Handle room counts
        Object bedroomsObj = propertyData.get("numBedrooms");
        if (bedroomsObj instanceof Number) {
            builder.numBedrooms(((Number) bedroomsObj).intValue());
        }

        Object bathroomsObj = propertyData.get("numBathrooms");
        if (bathroomsObj instanceof Number) {
            builder.numBathrooms(((Number) bathroomsObj).intValue());
        }

        // Handle location data
        Map<String, Object> locationData = (Map<String, Object>) propertyData.get("location");
        if (locationData != null) {
            Object latObj = locationData.get("latitude");
            Object lonObj = locationData.get("longitude");
            if (latObj instanceof Number && lonObj instanceof Number) {
                builder.location(new GeoPoint(
                    ((Number) latObj).doubleValue(),
                    ((Number) lonObj).doubleValue()
                ));
            }
        }

        // Handle posted date
        Object postedAtObj = propertyData.get("postedAt");
        if (postedAtObj instanceof String) {
            try {
                builder.postedAt(LocalDateTime.parse((String) postedAtObj));
            } catch (Exception e) {
                builder.postedAt(LocalDateTime.now());
            }
        } else {
            builder.postedAt(LocalDateTime.now());
        }

        // Extract city and district from address if available
        String address = (String) propertyData.get("address");
        if (address != null) {
            String[] addressParts = address.split(",");
            if (addressParts.length >= 2) {
                builder.city(addressParts[addressParts.length - 1].trim());
                if (addressParts.length >= 3) {
                    builder.district(addressParts[addressParts.length - 2].trim());
                }
            }
        }

        // Set default values for additional fields
        builder.propertyType((String) propertyData.getOrDefault("propertyType", "apartment"));
        builder.viewCount((Integer) propertyData.getOrDefault("viewCount", 0));
        builder.favoriteCount((Integer) propertyData.getOrDefault("favoriteCount", 0));
        builder.rating((Double) propertyData.getOrDefault("rating", 0.0));

        return builder.build();
    }

    /**
     * Convert user data to UserDocument
     */
    private UserDocument convertToUserDocument(Map<String, Object> userData) {
        UserDocument.UserDocumentBuilder builder = UserDocument.builder()
                .id(String.valueOf(userData.get("id")))
                .userId((String) userData.get("userId"))
                .name((String) userData.get("name"))
                .email((String) userData.get("email"))
                .role((String) userData.get("role"))
                .bio((String) userData.get("bio"))
                .company((String) userData.get("company"))
                .phone((String) userData.get("phone"))
                .location((String) userData.get("location"))
                .rating((Integer) userData.getOrDefault("rating", 0))
                .reviewCount((Integer) userData.getOrDefault("reviewCount", 0))
                .active((Boolean) userData.getOrDefault("active", true));

        // Handle specialties for agents
        Object specialtiesObj = userData.get("specialties");
        if (specialtiesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> specialties = (List<String>) specialtiesObj;
            builder.specialties(specialties);
        }

        // Handle timestamps
        Object createdAtObj = userData.get("createdAt");
        if (createdAtObj instanceof String) {
            try {
                builder.createdAt(LocalDateTime.parse((String) createdAtObj));
            } catch (Exception e) {
                builder.createdAt(LocalDateTime.now());
            }
        } else {
            builder.createdAt(LocalDateTime.now());
        }

        Object lastActiveObj = userData.get("lastActive");
        if (lastActiveObj instanceof String) {
            try {
                builder.lastActive(LocalDateTime.parse((String) lastActiveObj));
            } catch (Exception e) {
                builder.lastActive(LocalDateTime.now());
            }
        } else {
            builder.lastActive(LocalDateTime.now());
        }

        return builder.build();
    }

    /**
     * Publish search analytics event
     */
    public void publishSearchAnalytics(String searchQuery, int resultCount, long searchTime) {
        try {
            Map<String, Object> analyticsData = Map.of(
                "searchQuery", searchQuery,
                "resultCount", resultCount,
                "searchTime", searchTime,
                "timestamp", LocalDateTime.now().toString()
            );

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SEARCH_ANALYTICS_EXCHANGE,
                RabbitMQConfig.SEARCH_ANALYTICS_ROUTING_KEY,
                analyticsData
            );

        } catch (Exception e) {
            log.error("Error publishing search analytics", e);
        }
    }

    /**
     * Manual property indexing for bulk operations
     */
    public void bulkIndexProperties() {
        log.info("Starting bulk property indexing...");
        try {
            long propertyCount = propertySearchRepository.count();
            log.info("Current Elasticsearch property count: {}", propertyCount);
            
            // In a real implementation, you would fetch from the main database
            // For now, just log the operation
            log.info("Bulk property indexing completed");
            
        } catch (Exception e) {
            log.error("Error during bulk property indexing", e);
        }
    }

    /**
     * Manual user indexing for bulk operations
     */
    public void bulkIndexUsers() {
        log.info("Starting bulk user indexing...");
        try {
            long userCount = userSearchRepository.count();
            log.info("Current Elasticsearch user count: {}", userCount);
            
            // In a real implementation, you would fetch from the main database
            // For now, just log the operation
            log.info("Bulk user indexing completed");
            
        } catch (Exception e) {
            log.error("Error during bulk user indexing", e);
        }
    }

    /**
     * Get synchronization statistics
     */
    public Map<String, Object> getSyncStatistics() {
        try {
            long propertyCount = propertySearchRepository.count();
            long userCount = userSearchRepository.count();
            
            return Map.of(
                "elasticsearchPropertyCount", propertyCount,
                "elasticsearchUserCount", userCount,
                "lastSyncTime", LocalDateTime.now().toString()
            );
            
        } catch (Exception e) {
            log.error("Error getting sync statistics", e);
            return Map.of(
                "error", "Failed to get statistics: " + e.getMessage()
            );
        }
    }
} 