package com.team12.searchservice.service;

import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.dto.PropertySearchRequest;
import com.team12.searchservice.dto.SearchResponse;
import com.team12.searchservice.repository.PropertySearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PropertySearchServiceTest {

    @Mock
    private PropertySearchRepository repository;

    @InjectMocks
    private PropertySearchService service;

    private List<PropertyDocument> seed;

    @BeforeEach
    void setUp() {
        seed = new ArrayList<>();

        PropertyDocument a = PropertyDocument.builder()
                .id("1")
                .title("Cozy Orchard Condo")
                .description("Near MRT")
                .address("Orchard, Singapore")
                .city("Singapore")
                .price(BigDecimal.valueOf(2500))
                .numBedrooms(2)
                .numBathrooms(1)
                .available(true)
                .postedAt(LocalDateTime.now())
                .build();

        PropertyDocument b = PropertyDocument.builder()
                .id("2")
                .title("Spacious Downtown Apartment")
                .description("City view")
                .address("Downtown, Singapore")
                .city("Singapore")
                .price(BigDecimal.valueOf(5200))
                .numBedrooms(3)
                .numBathrooms(2)
                .available(true)
                .postedAt(LocalDateTime.now())
                .build();

        PropertyDocument c = PropertyDocument.builder()
                .id("3")
                .title("Suburban House")
                .description("Quiet area")
                .address("Woodlands, Singapore")
                .city("Singapore")
                .price(BigDecimal.valueOf(1800))
                .numBedrooms(2)
                .numBathrooms(2)
                .available(true)
                .postedAt(LocalDateTime.now())
                .build();

        seed.add(a); seed.add(b); seed.add(c);
    }

    @Test
    void searchPropertiesSimple_shouldFilterByKeywordAndPriceAndRooms() {
        // Setup: Only property with "Orchard" in title/address should match
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .keyword("Orchard")
                .minPrice(BigDecimal.valueOf(2000))
                .maxPrice(BigDecimal.valueOf(3000))
                .minBedrooms(2)
                .maxBedrooms(2)
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Only property "1" matches: contains "Orchard", price 2500 (2000-3000), 2 bedrooms
        assertThat(resp.getContent()).extracting(PropertyDocument::getId).containsExactly("1");
        assertThat(resp.getTotalElements()).isEqualTo(1L); // Only 1 property matches all criteria
        assertThat(resp.getPage()).isEqualTo(0);
    }

    @Test
    void searchPropertiesSimple_shouldHandleEmptyKeyword() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .keyword("") // Empty keyword
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Should return all properties when keyword is empty
        assertThat(resp.getContent()).hasSize(3);
        assertThat(resp.getTotalElements()).isEqualTo(3L);
    }

    @Test
    void searchPropertiesSimple_shouldHandleNullFilters() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .page(0)
                .size(20)
                .build(); // No filters

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Should return all properties when no filters applied
        assertThat(resp.getContent()).hasSize(3);
        assertThat(resp.getTotalElements()).isEqualTo(3L);
    }

    @Test
    void searchPropertiesSimple_shouldHandlePriceOnlyFilter() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .minPrice(BigDecimal.valueOf(2000))
                .maxPrice(BigDecimal.valueOf(3000))
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Only properties with price between 2000-3000: id1 (2500)
        assertThat(resp.getContent()).extracting(PropertyDocument::getId).containsExactly("1");
    }

    @Test
    void searchPropertiesSimple_shouldHandleBedroomFilter() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .minBedrooms(3)
                .maxBedrooms(3)
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Only property with 3 bedrooms: id2
        assertThat(resp.getContent()).extracting(PropertyDocument::getId).containsExactly("2");
    }

    @Test
    void searchPropertiesSimple_shouldHandleBathroomFilter() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .minBathrooms(2)
                .maxBathrooms(2)
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // Properties with 2 bathrooms: id2, id3
        assertThat(resp.getContent()).extracting(PropertyDocument::getId).containsExactlyInAnyOrder("2", "3");
    }

    @Test
    void searchPropertiesSimple_shouldHandleCityFilter() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .city("Singapore") // All properties have city="Singapore"
                .page(0)
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchPropertiesSimple(req);

        // All properties have city="Singapore"
        assertThat(resp.getContent()).extracting(PropertyDocument::getId).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void searchProperties_shouldReturnResponseWithAggregations() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);
        when(repository.countByCityAndAvailable(any(), eq(true))).thenReturn(1L);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .page(0) // First page should include aggregations
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchProperties(req);

        assertThat(resp.getContent()).hasSize(3);
        assertThat(resp.getCityAggregations()).isNotNull();
        assertThat(resp.getPriceRangeAggregations()).isNotNull();
    }

    @Test
    void searchProperties_shouldNotIncludeAggregationsForNonFirstPage() {
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(1, 20), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        PropertySearchRequest req = PropertySearchRequest.builder()
                .page(1) // Non-first page
                .size(20)
                .build();

        SearchResponse<PropertyDocument> resp = service.searchProperties(req);

        assertThat(resp.getContent()).hasSize(3);
        // Aggregations should be null for non-first page
        assertThat(resp.getCityAggregations()).isNull();
        assertThat(resp.getPriceRangeAggregations()).isNull();
    }

    @Test
    void getSearchStats_shouldReturnTotalsAndOccupancy() {
        when(repository.count()).thenReturn(100L);

        Map<String, Object> stats = service.getSearchStats();

        assertThat(stats.get("totalProperties")).isEqualTo(100L);
        assertThat(stats.get("availableProperties")).isEqualTo(80L); // 80% of total
        assertThat((Double) stats.get("occupancyRate")).isEqualTo(20.0); // 20% occupancy
    }

    @Test
    void getSearchStats_shouldHandleZeroProperties() {
        when(repository.count()).thenReturn(0L);

        Map<String, Object> stats = service.getSearchStats();

        assertThat(stats.get("totalProperties")).isEqualTo(0L);
        assertThat(stats.get("availableProperties")).isEqualTo(0L);
        assertThat((Double) stats.get("occupancyRate")).isEqualTo(0.0);
    }

    @Test
    void getPriceRangeAggregations_shouldBucketizePrices() {
        // Setup: Return all seed data for price range calculation
        Page<PropertyDocument> page = new PageImpl<>(seed, PageRequest.of(0, 1000), seed.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        Map<String, Long> buckets = service.getPriceRangeAggregations();

        // Expected distribution based on seed data:
        // id1: 2500 -> "2000-3000"
        // id2: 5200 -> "5000+"
        // id3: 1800 -> "1000-2000"
        assertThat(buckets.get("1000-2000")).isEqualTo(1L); // id3
        assertThat(buckets.get("2000-3000")).isEqualTo(1L); // id1
        assertThat(buckets.get("5000+")).isEqualTo(1L); // id2
        assertThat(buckets.get("0-1000")).isEqualTo(0L);
        assertThat(buckets.get("3000-5000")).isEqualTo(0L);
    }

    @Test
    void getPriceRangeAggregations_shouldHandleNullPrices() {
        // Create property with null price
        PropertyDocument nullPriceProperty = PropertyDocument.builder()
                .id("4")
                .title("Null Price Property")
                .price(null)
                .available(true)
                .build();
        
        List<PropertyDocument> testData = List.of(nullPriceProperty);
        Page<PropertyDocument> page = new PageImpl<>(testData, PageRequest.of(0, 1000), testData.size());
        when(repository.findByAvailable(eq(true), any())).thenReturn(page);

        Map<String, Long> buckets = service.getPriceRangeAggregations();

        // All buckets should be 0 for null price
        assertThat(buckets.get("0-1000")).isEqualTo(0L);
        assertThat(buckets.get("1000-2000")).isEqualTo(0L);
        assertThat(buckets.get("2000-3000")).isEqualTo(0L);
        assertThat(buckets.get("3000-5000")).isEqualTo(0L);
        assertThat(buckets.get("5000+")).isEqualTo(0L);
    }

    @Test
    void getCityAggregations_shouldReturnCityCounts() {
        when(repository.countByCityAndAvailable("Toronto", true)).thenReturn(10L);
        when(repository.countByCityAndAvailable("Vancouver", true)).thenReturn(5L);
        when(repository.countByCityAndAvailable("Montreal", true)).thenReturn(0L); // No properties

        Map<String, Long> cityStats = service.getCityAggregations();

        assertThat(cityStats.get("Toronto")).isEqualTo(10L);
        assertThat(cityStats.get("Vancouver")).isEqualTo(5L);
        assertThat(cityStats).doesNotContainKey("Montreal"); // Should not include cities with 0 count
    }

    @Test
    void getSuggestions_shouldReturnFilteredSuggestions() {
        List<String> suggestions = service.getSuggestions("ap", 5);

        assertThat(suggestions).contains("apartment");
        assertThat(suggestions).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void getSuggestions_shouldReturnEmptyListForEmptyKeyword() {
        List<String> suggestions = service.getSuggestions("", 5);

        assertThat(suggestions).isEmpty();
    }

    @Test
    void getSuggestions_shouldReturnEmptyListForNullKeyword() {
        List<String> suggestions = service.getSuggestions(null, 5);

        assertThat(suggestions).isEmpty();
    }

    @Test
    void getTrendingKeywords_shouldReturnLimitedResults() {
        List<String> trending = service.getTrendingKeywords(3);

        assertThat(trending).hasSize(3);
        assertThat(trending).containsAnyOf("apartment", "house", "condo", "luxury", "downtown");
    }

    @Test
    void getTrendingKeywords_shouldReturnAllWhenLimitExceedsAvailable() {
        List<String> trending = service.getTrendingKeywords(20);

        assertThat(trending).hasSizeLessThanOrEqualTo(15); // Available keywords in mock
    }
}

