package com.team12.searchservice.repository;

import com.team12.searchservice.document.PropertyDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertySearchRepository extends ElasticsearchRepository<PropertyDocument, String> {

    // Text search
    Page<PropertyDocument> findByTitleContainingOrDescriptionContainingOrAddressContaining(
            String title, String description, String address, Pageable pageable);

    // Location-based search
    Page<PropertyDocument> findByCityAndAvailable(String city, Boolean available, Pageable pageable);

    // Price range search
    Page<PropertyDocument> findByPriceBetweenAndAvailable(
            BigDecimal minPrice, BigDecimal maxPrice, Boolean available, Pageable pageable);

    // Property attributes search
    Page<PropertyDocument> findByNumBedroomsAndNumBathroomsAndAvailable(
            Integer bedrooms, Integer bathrooms, Boolean available, Pageable pageable);

    // Agent properties
    Page<PropertyDocument> findByAgentIdAndAvailable(String agentId, Boolean available, Pageable pageable);

    // Available properties
    Page<PropertyDocument> findByAvailable(Boolean available, Pageable pageable);

    // Custom search query for complex searches
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"description\", \"address\", \"city\"]}}], \"filter\": [{\"term\": {\"available\": true}}]}}")
    Page<PropertyDocument> findByKeywordWithBoost(String keyword, Pageable pageable);

    // Find properties by property type
    Page<PropertyDocument> findByPropertyTypeAndAvailable(String propertyType, Boolean available, Pageable pageable);

    // Find properties within bedroom range
    Page<PropertyDocument> findByNumBedroomsBetweenAndAvailable(
            Integer minBedrooms, Integer maxBedrooms, Boolean available, Pageable pageable);

    // Count properties by city
    Long countByCityAndAvailable(String city, Boolean available);

    // Find trending properties (high view count)
    List<PropertyDocument> findTop10ByAvailableOrderByViewCountDesc(Boolean available);
} 