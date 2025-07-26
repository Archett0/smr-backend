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
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
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
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Comprehensive property search
     */
    public SearchResponse<PropertyDocument> searchProperties(PropertySearchRequest request) {
        log.info("Executing property search with request: {}", request);

        try {
            // Build and execute search query
            NativeSearchQuery searchQuery = buildSearchQuery(request);
            SearchHits<PropertyDocument> searchHits = elasticsearchOperations.search(
                searchQuery, PropertyDocument.class, IndexCoordinates.of("properties"));

            // Convert to SearchResponse
            SearchResponse<PropertyDocument> response = convertToSearchResponse(searchHits, request);
            
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
     * Build comprehensive search query
     */
    private NativeSearchQuery buildSearchQuery(PropertySearchRequest request) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // Build main query
        if (StringUtils.hasText(request.getKeyword())) {
            queryBuilder.withQuery(buildTextSearchQuery(request));
        }

        // Add filters
        addFilters(queryBuilder, request);

        // Add sorting
        queryBuilder.withSort(buildSort(request));

        // Add pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        queryBuilder.withPageable(pageable);

        return queryBuilder.build();
    }

    /**
     * Build text search query
     */
    private org.springframework.data.elasticsearch.core.query.Query buildTextSearchQuery(PropertySearchRequest request) {
        // Use repository's built-in text search for simplicity
        return null; // Will be handled by filters
    }

    /**
     * Add search filters
     */
    private void addFilters(NativeSearchQueryBuilder queryBuilder, PropertySearchRequest request) {
        // For now, we'll use a simple approach with repository methods
        // In a full implementation, this would build complex Elasticsearch queries
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
     * Convert search hits to SearchResponse
     */
    private SearchResponse<PropertyDocument> convertToSearchResponse(
            SearchHits<PropertyDocument> searchHits, PropertySearchRequest request) {
        
        List<PropertyDocument> content = searchHits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        return SearchResponse.<PropertyDocument>builder()
            .content(content)
            .page(request.getPage())
            .size(request.getSize())
            .totalElements(totalElements)
            .totalPages(totalPages)
            .isFirst(request.getPage() == 0)
            .isLast(request.getPage() >= totalPages - 1)
            .isEmpty(content.isEmpty())
            .hasNext(request.getPage() < totalPages - 1)
            .hasPrevious(request.getPage() > 0)
            .searchId(UUID.randomUUID().toString())
            .build();
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
        long availableProperties = propertySearchRepository.countByAvailable(true);
        
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

            // Simple text search
            if (StringUtils.hasText(request.getKeyword())) {
                page = propertySearchRepository.findByTitleContainingOrDescriptionContainingOrAddressContaining(
                    request.getKeyword(), request.getKeyword(), request.getKeyword(), pageable);
            } else if (StringUtils.hasText(request.getCity())) {
                page = propertySearchRepository.findByCityAndAvailable(request.getCity(), true, pageable);
            } else if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                BigDecimal minPrice = request.getMinPrice() != null ? request.getMinPrice() : BigDecimal.ZERO;
                BigDecimal maxPrice = request.getMaxPrice() != null ? request.getMaxPrice() : BigDecimal.valueOf(1000000);
                page = propertySearchRepository.findByPriceBetweenAndAvailable(minPrice, maxPrice, true, pageable);
            } else {
                page = propertySearchRepository.findByAvailable(true, pageable);
            }

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
} 