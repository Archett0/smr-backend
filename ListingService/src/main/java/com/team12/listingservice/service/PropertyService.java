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
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final NotificationClient notificationClient;
    private final UserActionClient userActionClient;
    private final UserClient userClient;

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

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    public Property updateProperty(Long id, Property property) {
        return propertyRepository.findById(id)
                .map(existing -> {
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

                    return propertyRepository.save(existing);
                })
                .orElse(null);
    }

    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }

    public Property addProperty(Property property) {
        return property;
    }

}
