package com.team12.listingservice.service;

import com.team12.listingservice.model.Property;
import com.team12.listingservice.reponsitory.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findById(id);
    }

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    public Property updateProperty(Long id, Property property) {
        return propertyRepository.findById(id).map(existing -> {
            existing.setTitle(property.getTitle());
            existing.setDescription(property.getDescription());
            existing.setPrice(property.getPrice());
            existing.setAddress(property.getAddress());
            existing.setImg(property.getImg());
            existing.setLocation(property.getLocation());
            existing.setNumBedrooms(property.getNumBedrooms());
            existing.setNumBathrooms(property.getNumBathrooms());
            existing.setAvailable(property.isAvailable());
            return propertyRepository.save(existing);
        }).orElse(null);
    }

    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }

    public Property addProperty(Property property) {
        return property;
    }
}
