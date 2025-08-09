package com.team12.listingservice.reponsitory;

import com.team12.listingservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    /**
     * Count properties that are available
     */
    long countByAvailableTrue();
    
    /**
     * Count properties that are not available
     */
    long countByAvailableFalse();
    
    /**
     * Find properties by agent ID
     */
    java.util.List<Property> findByAgentId(String agentId);
    
    /**
     * Find available properties
     */
    java.util.List<Property> findByAvailableTrue();
}
