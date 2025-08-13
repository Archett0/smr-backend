package com.team12.searchservice.controller;

import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.dto.PropertySearchRequest;
import com.team12.searchservice.dto.SearchResponse;
import com.team12.searchservice.service.PropertySearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PropertySearchControllerTest {

    @Mock
    private PropertySearchService propertySearchService;

    @InjectMocks
    private PropertySearchController propertySearchController;

    private PropertyDocument testProperty;
    private SearchResponse<PropertyDocument> testResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProperty = PropertyDocument.builder()
                .id("1")
                .title("Test Apartment")
                .description("A beautiful apartment")
                .address("123 Test Street, Singapore")
                .city("Singapore")
                .price(BigDecimal.valueOf(2500))
                .numBedrooms(2)
                .numBathrooms(1)
                .available(true)
                .postedAt(LocalDateTime.now())
                .build();

        testResponse = SearchResponse.<PropertyDocument>builder()
                .content(Arrays.asList(testProperty))
                .totalElements(1L)
                .page(0)
                .size(20)
                .searchId("test-search-id")
                .build();
    }

    @Test
    void searchProperties_ShouldReturnSearchResults() {
        // Given
        PropertySearchRequest request = PropertySearchRequest.builder()
                .keyword("apartment")
                .page(0)
                .size(20)
                .build();

        when(propertySearchService.searchProperties(any(PropertySearchRequest.class)))
                .thenReturn(testResponse);

        // When
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.searchProperties(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1L);
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo("1");
        assertThat(response.getBody().getSearchId()).isEqualTo("test-search-id");
    }

    @Test
    void searchPropertiesSimple_ShouldHandleQueryParameters() {
        // Given
        when(propertySearchService.searchPropertiesSimple(any(PropertySearchRequest.class)))
                .thenReturn(testResponse);

        // When
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.searchPropertiesSimple(
                        "downtown", "Singapore", "apartment", 
                        2000.0, 4000.0, 2, 3, 1, 2, 
                        "price", "asc", 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1L);
    }

    @Test
    void searchPropertiesSimple_ShouldUseDefaultParameters() {
        // Given
        SearchResponse<PropertyDocument> emptyResponse = SearchResponse.<PropertyDocument>builder()
                .content(Arrays.asList())
                .totalElements(0L)
                .page(0)
                .size(20)
                .build();

        when(propertySearchService.searchPropertiesSimple(any(PropertySearchRequest.class)))
                .thenReturn(emptyResponse);

        // When - should use defaults: sortBy=postedAt, sortOrder=desc, page=0, size=20
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.searchPropertiesSimple(
                        null, null, null, null, null, null, null, null, null, 
                        null, null, null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0L);
    }

    @Test
    void findNearbyProperties_ShouldReturnNearbyResults() {
        // Given
        when(propertySearchService.searchProperties(any(PropertySearchRequest.class)))
                .thenReturn(testResponse);

        // When
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.findNearbyProperties(1.3521, 103.8198, 5.0, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1L);
    }

    @Test
    void findNearbyProperties_ShouldUseDefaultRadius() {
        // Given
        SearchResponse<PropertyDocument> emptyResponse = SearchResponse.<PropertyDocument>builder()
                .content(Arrays.asList())
                .totalElements(0L)
                .page(0)
                .size(20)
                .build();

        when(propertySearchService.searchProperties(any(PropertySearchRequest.class)))
                .thenReturn(emptyResponse);

        // When - should use default radius of 5.0km
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.findNearbyProperties(1.3521, 103.8198, null, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0L);
    }

    @Test
    void getSearchStats_ShouldReturnStatistics() {
        // Given
        Map<String, Object> stats = Map.of(
                "totalProperties", 100L,
                "availableProperties", 80L,
                "occupancyRate", 20.0,
                "lastUpdated", LocalDateTime.now()
        );

        when(propertySearchService.getSearchStats()).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = propertySearchController.getSearchStats();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalProperties")).isEqualTo(100L);
        assertThat(response.getBody().get("availableProperties")).isEqualTo(80L);
        assertThat(response.getBody().get("occupancyRate")).isEqualTo(20.0);
    }

    @Test
    void getCityAggregations_ShouldReturnCityCounts() {
        // Given
        Map<String, Long> cityStats = Map.of(
                "Singapore", 50L,
                "Toronto", 30L,
                "Vancouver", 20L
        );

        when(propertySearchService.getCityAggregations()).thenReturn(cityStats);

        // When
        ResponseEntity<Map<String, Long>> response = propertySearchController.getCityAggregations();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("Singapore")).isEqualTo(50L);
        assertThat(response.getBody().get("Toronto")).isEqualTo(30L);
        assertThat(response.getBody().get("Vancouver")).isEqualTo(20L);
    }

    @Test
    void getPriceRangeAggregations_ShouldReturnPriceBuckets() {
        // Given
        Map<String, Long> priceRanges = Map.of(
                "1000-2000", 25L,
                "2000-3000", 30L,
                "3000-5000", 20L,
                "5000+", 15L
        );

        when(propertySearchService.getPriceRangeAggregations()).thenReturn(priceRanges);

        // When
        ResponseEntity<Map<String, Long>> response = propertySearchController.getPriceRangeAggregations();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("1000-2000")).isEqualTo(25L);
        assertThat(response.getBody().get("2000-3000")).isEqualTo(30L);
        assertThat(response.getBody().get("3000-5000")).isEqualTo(20L);
        assertThat(response.getBody().get("5000+")).isEqualTo(15L);
    }

    @Test
    void getTrendingKeywords_ShouldReturnKeywords() {
        // Given
        List<String> trending = Arrays.asList("apartment", "house", "condo", "luxury");

        when(propertySearchService.getTrendingKeywords(10)).thenReturn(trending);

        // When
        ResponseEntity<List<String>> response = propertySearchController.getTrendingKeywords(10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(4);
        assertThat(response.getBody()).contains("apartment", "house", "condo", "luxury");
    }

    @Test
    void getTrendingKeywords_ShouldUseDefaultLimit() {
        // Given
        List<String> trending = Arrays.asList("apartment", "house");

        when(propertySearchService.getTrendingKeywords(any())).thenReturn(trending);

        // When - should use default limit of 10
        ResponseEntity<List<String>> response = propertySearchController.getTrendingKeywords(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).contains("apartment", "house");
    }

    @Test
    void getSuggestions_ShouldReturnSuggestions() {
        // Given
        List<String> suggestions = Arrays.asList("apartment", "apartments");

        when(propertySearchService.getSuggestions("ap", 5)).thenReturn(suggestions);

        // When
        ResponseEntity<List<String>> response = propertySearchController.getSuggestions("ap", 5);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).contains("apartment", "apartments");
    }

    @Test
    void getSuggestions_ShouldUseDefaultSize() {
        // Given
        List<String> suggestions = Arrays.asList("apartment");

        when(propertySearchService.getSuggestions(any(), any())).thenReturn(suggestions);

        // When - should use default size of 10
        ResponseEntity<List<String>> response = propertySearchController.getSuggestions("ap", null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains("apartment");
    }

    @Test
    void getSuggestions_ShouldReturnEmptyList_WhenKeywordIsEmpty() {
        // Given
        when(propertySearchService.getSuggestions("", 10)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<String>> response = propertySearchController.getSuggestions("", null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void searchProperties_ShouldHandleNullRequest() {
        // Given
        SearchResponse<PropertyDocument> emptyResponse = SearchResponse.<PropertyDocument>builder()
                .content(Arrays.asList())
                .totalElements(0L)
                .page(0)
                .size(20)
                .searchId("empty-search-id")
                .build();
        
        when(propertySearchService.searchProperties(any(PropertySearchRequest.class)))
                .thenReturn(emptyResponse);

        // When
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.searchProperties(PropertySearchRequest.builder().build());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void searchProperties_ShouldIncludeSearchTime() {
        // Given
        when(propertySearchService.searchProperties(any(PropertySearchRequest.class)))
                .thenReturn(testResponse);

        // When
        ResponseEntity<SearchResponse<PropertyDocument>> response = 
                propertySearchController.searchProperties(PropertySearchRequest.builder().build());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSearchTime()).isGreaterThanOrEqualTo(0L);
    }
} 