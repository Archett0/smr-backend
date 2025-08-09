package com.team12.searchservice.service;

import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.dto.PropertySearchRequest;
import com.team12.searchservice.dto.SearchResponse;
import com.team12.searchservice.repository.PropertySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final PropertySearchRepository propertySearchRepository;

    /**
     * Comprehensive property search
     */
    public SearchResponse<PropertyDocument> searchProperties(PropertySearchRequest request) {
        log.info("Executing property search with request: {}", request);

        try {
            // Use the simpler searchPropertiesSimple method for now
            // This avoids the complex query building that has compatibility issues
            SearchResponse<PropertyDocument> response = searchPropertiesSimple(request);
            
            // Add aggregations for first page
            if (request.getPage() == 0) {
                addAggregations(response);
            }

            log.info("Search completed, found {} results", response.getTotalElements());
            return response;
            
        } catch (Exception e) {
            log.error("Error occurred while searching properties", e);
            return createEmptyResponse(request);
        }
    }

    /**
     * Build sort criteria
     */
    private Sort buildSort(PropertySearchRequest request) {
        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "postedAt";
        String sortOrder = StringUtils.hasText(request.getSortOrder()) ? request.getSortOrder() : "desc";
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? 
            Sort.Direction.ASC : Sort.Direction.DESC;

        switch (sortBy.toLowerCase()) {
            case "price":
                return Sort.by(direction, "price");
            case "postedat":
                return Sort.by(direction, "postedAt");
            case "rating":
                return Sort.by(direction, "rating");
            default:
                return Sort.by(direction, "postedAt");
        }
    }



    /**
     * Create empty response for error cases
     */
    private SearchResponse<PropertyDocument> createEmptyResponse(PropertySearchRequest request) {
        return SearchResponse.<PropertyDocument>builder()
            .content(Collections.emptyList())
            .page(request.getPage())
            .size(request.getSize())
            .totalElements(0L)
            .totalPages(0)
            .isFirst(true)
            .isLast(true)
            .isEmpty(true)
            .hasNext(false)
            .hasPrevious(false)
            .searchId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Add aggregations to response
     */
    private void addAggregations(SearchResponse<PropertyDocument> response) {
        response.setCityAggregations(getCityAggregations());
        response.setPriceRangeAggregations(getPriceRangeAggregations());
    }

    /**
     * Build flexible search that can combine multiple criteria
     */
    private Page<PropertyDocument> buildFlexibleSearch(PropertySearchRequest request, Pageable pageable) {
        try {
            // Start with all available properties
            Page<PropertyDocument> page = propertySearchRepository.findByAvailable(true, pageable);
            
            // Apply filters sequentially
            if (StringUtils.hasText(request.getKeyword())) {
                page = applyKeywordFilter(page, request.getKeyword());
            }
            
            if (StringUtils.hasText(request.getCity())) {
                page = applyCityFilter(page, request.getCity());
            }
            
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                page = applyPriceFilter(page, request.getMinPrice(), request.getMaxPrice());
            }
            
            if (request.getMinBedrooms() != null || request.getMaxBedrooms() != null) {
                page = applyBedroomFilter(page, request.getMinBedrooms(), request.getMaxBedrooms());
            }
            
            if (request.getMinBathrooms() != null || request.getMaxBathrooms() != null) {
                page = applyBathroomFilter(page, request.getMinBathrooms(), request.getMaxBathrooms());
            }
            
            return page;
            
        } catch (Exception e) {
            log.error("Error in flexible search", e);
            // Fallback to simple available search
            return propertySearchRepository.findByAvailable(true, pageable);
        }
    }

    /**
     * Search properties by bedroom and bathroom range
     */
    private Page<PropertyDocument> searchByBedroomAndBathroomRange(
            Integer minBedrooms, Integer maxBedrooms, 
            Integer minBathrooms, Integer maxBathrooms, 
            Pageable pageable) {
        
        try {
            // First try to find properties within the bedroom range
            Page<PropertyDocument> bedroomResults = propertySearchRepository
                .findByNumBedroomsBetweenAndAvailable(minBedrooms, maxBedrooms, true, pageable);
            
            // If we have results, filter by bathroom range
            if (!bedroomResults.isEmpty()) {
                List<PropertyDocument> filteredResults = bedroomResults.getContent().stream()
                    .filter(property -> {
                        Integer bathrooms = property.getNumBathrooms();
                        return bathrooms != null && bathrooms >= minBathrooms && bathrooms <= maxBathrooms;
                    })
                    .collect(Collectors.toList());
                
                // Create a new page with filtered results
                return new org.springframework.data.domain.PageImpl<>(
                    filteredResults, pageable, bedroomResults.getTotalElements());
            }
            
            return bedroomResults;
            
        } catch (Exception e) {
            log.error("Error searching by bedroom and bathroom range", e);
            // Fallback to simple available search
            return propertySearchRepository.findByAvailable(true, pageable);
        }
    }

    /**
     * Get search suggestions
     */
    public List<String> getSuggestions(String keyword, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        // Simple implementation - in practice, this would use Elasticsearch suggestions
        List<String> suggestions = Arrays.asList(
            "apartment", "house", "condo", "luxury", "downtown",
            "parking", "furnished", "pool", "gym", "balcony"
        );
        
        return suggestions.stream()
            .filter(s -> s.toLowerCase().contains(keyword.toLowerCase()))
            .limit(size)
            .collect(Collectors.toList());
    }

    /**
     * Get search statistics
     */
    public Map<String, Object> getSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalProperties = propertySearchRepository.count();
        // For now, use a simple estimate
        long availableProperties = (long)(totalProperties * 0.8); // Assume 80% available
        
        stats.put("totalProperties", totalProperties);
        stats.put("availableProperties", availableProperties);
        stats.put("occupancyRate", totalProperties > 0 ? 
            (double)(totalProperties - availableProperties) / totalProperties * 100 : 0.0);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }

    /**
     * Get trending keywords
     */
    @Cacheable(value = "trendingKeywords", key = "'trending'")
    public List<String> getTrendingKeywords(Integer limit) {
        // Mock implementation - in practice, this would come from search analytics
        List<String> trending = Arrays.asList(
            "apartment", "house", "condo", "luxury", "downtown",
            "parking", "furnished", "pool", "gym", "balcony",
            "modern", "spacious", "quiet", "transport", "shopping"
        );
        
        return trending.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get city aggregations
     */
    @Cacheable(value = "cityAggregations", key = "'cities'")
    public Map<String, Long> getCityAggregations() {
        Map<String, Long> cityStats = new HashMap<>();
        
        // Sample cities - in practice, this would come from Elasticsearch aggregations
        List<String> cities = Arrays.asList("Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa");
        for (String city : cities) {
            Long count = propertySearchRepository.countByCityAndAvailable(city, true);
            if (count != null && count > 0) {
                cityStats.put(city, count);
            }
        }
        
        return cityStats;
    }

    /**
     * Get price range aggregations
     */
    @Cacheable(value = "priceRangeAggregations", key = "'price-ranges'")
    public Map<String, Long> getPriceRangeAggregations() {
        Map<String, Long> priceRanges = new HashMap<>();
        
        // Initialize ranges
        priceRanges.put("0-1000", 0L);
        priceRanges.put("1000-2000", 0L);
        priceRanges.put("2000-3000", 0L);
        priceRanges.put("3000-5000", 0L);
        priceRanges.put("5000+", 0L);
        
        // Get sample data - in practice, this would use Elasticsearch aggregations
        try {
            Page<PropertyDocument> properties = propertySearchRepository.findByAvailable(true, PageRequest.of(0, 1000));
            
            for (PropertyDocument property : properties.getContent()) {
                BigDecimal price = property.getPrice();
                if (price != null) {
                    if (price.compareTo(BigDecimal.valueOf(1000)) < 0) {
                        priceRanges.merge("0-1000", 1L, Long::sum);
                    } else if (price.compareTo(BigDecimal.valueOf(2000)) < 0) {
                        priceRanges.merge("1000-2000", 1L, Long::sum);
                    } else if (price.compareTo(BigDecimal.valueOf(3000)) < 0) {
                        priceRanges.merge("2000-3000", 1L, Long::sum);
                    } else if (price.compareTo(BigDecimal.valueOf(5000)) < 0) {
                        priceRanges.merge("3000-5000", 1L, Long::sum);
                    } else {
                        priceRanges.merge("5000+", 1L, Long::sum);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error calculating price range aggregations", e);
        }
        
        return priceRanges;
    }

    /**
     * Search properties using repository methods for complex queries
     */
    public SearchResponse<PropertyDocument> searchPropertiesSimple(PropertySearchRequest request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), buildSort(request));
            Page<PropertyDocument> page;

            // Build search criteria based on request parameters
            // Use a more flexible approach that can combine multiple criteria
            page = buildFlexibleSearch(request, pageable);

            return SearchResponse.<PropertyDocument>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .isEmpty(page.isEmpty())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .searchId(UUID.randomUUID().toString())
                .build();

        } catch (Exception e) {
            log.error("Error in simple property search", e);
            return createEmptyResponse(request);
        }
    }

    /**
     * Apply keyword filter to search results
     */
    private Page<PropertyDocument> applyKeywordFilter(Page<PropertyDocument> page, String keyword) {
        List<PropertyDocument> filteredResults = page.getContent().stream()
            .filter(property -> {
                String title = property.getTitle() != null ? property.getTitle().toLowerCase() : "";
                String description = property.getDescription() != null ? property.getDescription().toLowerCase() : "";
                String address = property.getAddress() != null ? property.getAddress().toLowerCase() : "";
                String city = property.getCity() != null ? property.getCity().toLowerCase() : "";
                
                String searchKeyword = keyword.toLowerCase();
                return title.contains(searchKeyword) || 
                       description.contains(searchKeyword) || 
                       address.contains(searchKeyword) || 
                       city.contains(searchKeyword);
            })
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            filteredResults, page.getPageable(), page.getTotalElements());
    }

    /**
     * Apply city filter to search results
     */
    private Page<PropertyDocument> applyCityFilter(Page<PropertyDocument> page, String city) {
        List<PropertyDocument> filteredResults = page.getContent().stream()
            .filter(property -> {
                String propertyCity = property.getCity() != null ? property.getCity().toLowerCase() : "";
                return propertyCity.contains(city.toLowerCase());
            })
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            filteredResults, page.getPageable(), page.getTotalElements());
    }

    /**
     * Apply price filter to search results
     */
    private Page<PropertyDocument> applyPriceFilter(Page<PropertyDocument> page, BigDecimal minPrice, BigDecimal maxPrice) {
        List<PropertyDocument> filteredResults = page.getContent().stream()
            .filter(property -> {
                BigDecimal price = property.getPrice();
                if (price == null) return false;
                
                boolean minOk = minPrice == null || price.compareTo(minPrice) >= 0;
                boolean maxOk = maxPrice == null || price.compareTo(maxPrice) <= 0;
                
                return minOk && maxOk;
            })
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            filteredResults, page.getPageable(), page.getTotalElements());
    }

    /**
     * Apply bedroom filter to search results
     */
    private Page<PropertyDocument> applyBedroomFilter(Page<PropertyDocument> page, Integer minBedrooms, Integer maxBedrooms) {
        List<PropertyDocument> filteredResults = page.getContent().stream()
            .filter(property -> {
                Integer bedrooms = property.getNumBedrooms();
                if (bedrooms == null) return false;
                
                boolean minOk = minBedrooms == null || bedrooms >= minBedrooms;
                boolean maxOk = maxBedrooms == null || bedrooms <= maxBedrooms;
                
                return minOk && maxOk;
            })
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            filteredResults, page.getPageable(), page.getTotalElements());
    }

    /**
     * Apply bathroom filter to search results
     */
    private Page<PropertyDocument> applyBathroomFilter(Page<PropertyDocument> page, Integer minBathrooms, Integer maxBathrooms) {
        List<PropertyDocument> filteredResults = page.getContent().stream()
            .filter(property -> {
                Integer bathrooms = property.getNumBathrooms();
                if (bathrooms == null) return false;
                
                boolean minOk = minBathrooms == null || bathrooms >= minBathrooms;
                boolean maxOk = maxBathrooms == null || bathrooms <= maxBathrooms;
                
                return minOk && maxOk;
            })
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            filteredResults, page.getPageable(), page.getTotalElements());
    }
} 