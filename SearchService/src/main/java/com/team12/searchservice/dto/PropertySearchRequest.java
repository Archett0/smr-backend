package com.team12.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchRequest {

    // Text search
    private String keyword;
    
    // Location filters
    private String city;
    private String district;
    private String address;
    
    // Geographic search
    private Double latitude;
    private Double longitude;
    @DecimalMin(value = "0.1", message = "Radius must be at least 0.1 km")
    @Max(value = 50, message = "Radius cannot exceed 50 km")
    private Double radiusKm;
    
    // Property attributes
    @Min(value = 0, message = "Number of bedrooms cannot be negative")
    private Integer minBedrooms;
    @Min(value = 0, message = "Number of bedrooms cannot be negative")
    private Integer maxBedrooms;
    
    @Min(value = 0, message = "Number of bathrooms cannot be negative")
    private Integer minBathrooms;
    @Min(value = 0, message = "Number of bathrooms cannot be negative")
    private Integer maxBathrooms;
    
    // Price range
    @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Property type
    private String propertyType;
    
    // Availability
    private Boolean available;
    
    // Date filters
    private String postedAfter; // ISO 8601 format
    private String postedBefore; // ISO 8601 format
    
    // Agent filter
    private String agentId;
    
    // Additional filters
    @DecimalMin(value = "0.0", message = "Minimum rating cannot be negative")
    @Max(value = 5, message = "Maximum rating cannot exceed 5")
    private Double minRating;
    
    private List<String> amenities;
    
    // Sorting options
    private String sortBy; // price, postedAt, rating, distance
    private String sortOrder; // asc, desc
    
    // Pagination
    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;
    
    // Search mode
    @Builder.Default
    private Boolean fuzzySearch = false;
    @Builder.Default
    private Boolean highlightResults = false;
} 