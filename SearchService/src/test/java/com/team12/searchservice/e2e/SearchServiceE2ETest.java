package com.team12.searchservice.e2e;

import com.team12.searchservice.document.PropertyDocument;
import com.team12.searchservice.dto.PropertySearchRequest;
import com.team12.searchservice.dto.SearchResponse;
import com.team12.searchservice.repository.PropertySearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SearchServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PropertySearchRepository propertySearchRepository;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        // 清理测试数据
        propertySearchRepository.deleteAll();
        
        // 插入测试数据
        insertTestData();
    }

    private void insertTestData() {
        List<PropertyDocument> properties = Arrays.asList(
                PropertyDocument.builder()
                        .id("1")
                        .title("Modern Apartment in Orchard")
                        .description("Beautiful modern apartment in the heart of Orchard")
                        .address("123 Orchard Road, Singapore")
                        .city("Singapore")
                        .price(new BigDecimal("3500.0"))
                        .numBedrooms(2)
                        .numBathrooms(2)
                        .propertyType("Apartment")
                        .available(true)
                        .location(new GeoPoint(1.3521, 103.8198))
                        .build(),
                PropertyDocument.builder()
                        .id("2")
                        .title("Luxury Condo in Marina Bay")
                        .description("Luxurious condo with marina view")
                        .address("456 Marina Bay Sands, Singapore")
                        .city("Singapore")
                        .price(new BigDecimal("8000.0"))
                        .numBedrooms(3)
                        .numBathrooms(3)
                        .propertyType("Condo")
                        .available(true)
                        .location(new GeoPoint(1.2867, 103.8500))
                        .build(),
                PropertyDocument.builder()
                        .id("3")
                        .title("Cozy Studio in Chinatown")
                        .description("Cozy studio apartment in historic Chinatown")
                        .address("789 Chinatown Street, Singapore")
                        .city("Singapore")
                        .price(new BigDecimal("2000.0"))
                        .numBedrooms(1)
                        .numBathrooms(1)
                        .propertyType("Studio")
                        .available(false)
                        .location(new GeoPoint(1.2838, 103.8441))
                        .build()
        );

        propertySearchRepository.saveAll(properties);
    }

    @Test
    void testBasicSearchEndpoint() {
        // Given
        PropertySearchRequest request = PropertySearchRequest.builder()
                .keyword("modern")
                .page(0)
                .size(10)
                .available(true)
                .build();

        HttpEntity<PropertySearchRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/search/properties/search",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        // Parse JSON response manually to verify structure
        String responseBody = response.getBody();
        assertThat(responseBody).contains("Modern");
        assertThat(responseBody).contains("content");
        assertThat(responseBody).contains("totalElements");
    }

    @Test
    void testPriceRangeSearch() {
        // Given
        PropertySearchRequest request = PropertySearchRequest.builder()
                .minPrice(new BigDecimal("3000.0"))
                .maxPrice(new BigDecimal("5000.0"))
                .page(0)
                .size(10)
                .available(true)
                .build();

        HttpEntity<PropertySearchRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/search/properties/search",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        // Parse JSON response manually to verify structure
        String responseBody = response.getBody();
        assertThat(responseBody).contains("3500");
        assertThat(responseBody).contains("content");
        assertThat(responseBody).contains("totalElements");
    }

    @Test
    void testBedroomFilter() {
        // Given
        PropertySearchRequest request = PropertySearchRequest.builder()
                .minBedrooms(2)
                .maxBedrooms(3)
                .page(0)
                .size(10)
                .available(true)
                .build();

        HttpEntity<PropertySearchRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/search/properties/search",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        
        // Parse JSON response manually to verify structure
        String responseBody = response.getBody();
        assertThat(responseBody).contains("content");
        assertThat(responseBody).contains("totalElements");
        assertThat(responseBody).contains("numBedrooms");
    }

    @Test
    void testPublicAggregationsEndpoint() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/search/properties/aggregations/cities",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        // Should contain some city data
        assertThat(response.getBody()).contains("{");
    }
} 