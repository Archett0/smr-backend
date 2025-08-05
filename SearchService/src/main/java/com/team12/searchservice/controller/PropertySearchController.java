package com.team12.searchservice.controller;

import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.dto.PropertySearchRequest;
import com.team12.searchservice.dto.SearchResponse;
import com.team12.searchservice.service.PropertySearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/search/properties")
@RequiredArgsConstructor
public class PropertySearchController {

    private final PropertySearchService propertySearchService;

    /**
     * Comprehensive property search
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse<PropertyDocument>> searchProperties(
            @Valid @RequestBody PropertySearchRequest request) {
        
        log.info("Executing property search: {}", request);
        long startTime = System.currentTimeMillis();
        
        SearchResponse<PropertyDocument> response = propertySearchService.searchProperties(request);
        response.setSearchTime(System.currentTimeMillis() - startTime);
        
        log.info("Search completed, found {} results in {}ms", 
                response.getTotalElements(), response.getSearchTime());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Simple property search using query parameters
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse<PropertyDocument>> searchPropertiesSimple(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer maxBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Integer maxBathrooms,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        PropertySearchRequest request = PropertySearchRequest.builder()
                .keyword(keyword)
                .city(city)
                .propertyType(propertyType)
                .minPrice(minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null)
                .maxPrice(maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null)
                .minBedrooms(minBedrooms)
                .maxBedrooms(maxBedrooms)
                .minBathrooms(minBathrooms)
                .maxBathrooms(maxBathrooms)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .available(true)
                .build();
        
        return ResponseEntity.ok(propertySearchService.searchPropertiesSimple(request));
    }

    /**
     * Geographic location search
     */
    @GetMapping("/nearby")
    public ResponseEntity<SearchResponse<PropertyDocument>> findNearbyProperties(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        PropertySearchRequest request = PropertySearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusKm(radiusKm)
                .page(page)
                .size(size)
                .available(true)
                .build();
        
        return ResponseEntity.ok(propertySearchService.searchProperties(request));
    }

    /**
     * Keyword search suggestions
     */
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<String> suggestions = propertySearchService.getSuggestions(keyword, size);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Search statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats() {
        Map<String, Object> stats = propertySearchService.getSearchStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Trending search keywords
     */
    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingKeywords(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<String> trending = propertySearchService.getTrendingKeywords(limit);
        return ResponseEntity.ok(trending);
    }

    /**
     * City aggregations
     */
    @GetMapping("/aggregations/cities")
    public ResponseEntity<Map<String, Long>> getCityAggregations() {
        Map<String, Long> cityStats = propertySearchService.getCityAggregations();
        return ResponseEntity.ok(cityStats);
    }

    /**
     * Price range aggregations
     */
    @GetMapping("/aggregations/price-ranges")
    public ResponseEntity<Map<String, Long>> getPriceRangeAggregations() {
        Map<String, Long> priceRanges = propertySearchService.getPriceRangeAggregations();
        return ResponseEntity.ok(priceRanges);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "SearchService",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
} 