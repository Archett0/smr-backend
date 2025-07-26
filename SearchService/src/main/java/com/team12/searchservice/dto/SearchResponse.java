package com.team12.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T> {

    // Result data
    private List<T> content;
    
    // Pagination info
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean isFirst;
    private Boolean isLast;
    private Boolean isEmpty;
    
    // Search metadata
    private Long searchTime; // in milliseconds
    private String searchId; // for tracking
    private Boolean hasNext;
    private Boolean hasPrevious;
    
    // Aggregations and facets
    private Map<String, Long> cityAggregations;
    private Map<String, Long> priceRangeAggregations;
    private Map<String, Long> propertyTypeAggregations;
    private Map<String, Long> bedroomAggregations;
    private Map<String, Long> bathroomAggregations;
    
    // Search suggestions
    private List<String> suggestions;
    
    // Highlighting results (if enabled)
    private Map<String, List<String>> highlights;
    
    // Search statistics
    private Double averagePrice;
    private Double minPrice;
    private Double maxPrice;
    
    // Geospatial info
    private Double centerLatitude;
    private Double centerLongitude;
    private Double searchRadius;
} 