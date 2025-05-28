package com.team12.listingservice.reponsitory;

import com.team12.listingservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
