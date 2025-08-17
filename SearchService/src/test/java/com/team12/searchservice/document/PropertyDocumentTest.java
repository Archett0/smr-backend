package com.team12.searchservice.document;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyDocumentTest {

    @Test
    void builder_ShouldCreatePropertyDocumentWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        GeoPoint location = new GeoPoint(1.3521, 103.8198);

        // When
        PropertyDocument property = PropertyDocument.builder()
                .id("1")
                .title("Modern Apartment")
                .description("Beautiful modern apartment in the heart of Singapore")
                .price(new BigDecimal("3500.00"))
                .address("123 Orchard Road, Singapore")
                .img("apartment.jpg")
                .location(location)
                .numBedrooms(2)
                .numBathrooms(2)
                .available(true)
                .postedAt(now)
                .agentId("agent1")
                .propertyType("Apartment")
                .city("Singapore")
                .district("Central")
                .viewCount(150)
                .favoriteCount(25)
                .rating(4.5)
                .lastUpdated(now)
                .build();

        // Then
        assertThat(property.getId()).isEqualTo("1");
        assertThat(property.getTitle()).isEqualTo("Modern Apartment");
        assertThat(property.getDescription()).isEqualTo("Beautiful modern apartment in the heart of Singapore");
        assertThat(property.getPrice()).isEqualTo(new BigDecimal("3500.00"));
        assertThat(property.getAddress()).isEqualTo("123 Orchard Road, Singapore");
        assertThat(property.getImg()).isEqualTo("apartment.jpg");
        assertThat(property.getLocation()).isEqualTo(location);
        assertThat(property.getNumBedrooms()).isEqualTo(2);
        assertThat(property.getNumBathrooms()).isEqualTo(2);
        assertThat(property.getAvailable()).isTrue();
        assertThat(property.getPostedAt()).isEqualTo(now);
        assertThat(property.getAgentId()).isEqualTo("agent1");
        assertThat(property.getPropertyType()).isEqualTo("Apartment");
        assertThat(property.getCity()).isEqualTo("Singapore");
        assertThat(property.getDistrict()).isEqualTo("Central");
        assertThat(property.getViewCount()).isEqualTo(150);
        assertThat(property.getFavoriteCount()).isEqualTo(25);
        assertThat(property.getRating()).isEqualTo(4.5);
        assertThat(property.getLastUpdated()).isEqualTo(now);
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyPropertyDocument() {
        // When
        PropertyDocument property = new PropertyDocument();

        // Then
        assertThat(property.getId()).isNull();
        assertThat(property.getTitle()).isNull();
        assertThat(property.getDescription()).isNull();
        assertThat(property.getPrice()).isNull();
        assertThat(property.getAddress()).isNull();
        assertThat(property.getImg()).isNull();
        assertThat(property.getLocation()).isNull();
        assertThat(property.getNumBedrooms()).isNull();
        assertThat(property.getNumBathrooms()).isNull();
        assertThat(property.getAvailable()).isNull();
        assertThat(property.getPostedAt()).isNull();
        assertThat(property.getAgentId()).isNull();
        assertThat(property.getPropertyType()).isNull();
        assertThat(property.getCity()).isNull();
        assertThat(property.getDistrict()).isNull();
        assertThat(property.getViewCount()).isNull();
        assertThat(property.getFavoriteCount()).isNull();
        assertThat(property.getRating()).isNull();
        assertThat(property.getLastUpdated()).isNull();
    }

    @Test
    void setLocation_ShouldCreateGeoPointFromLatitudeAndLongitude() {
        // Given
        PropertyDocument property = new PropertyDocument();
        Double latitude = 1.3521;
        Double longitude = 103.8198;

        // When
        property.setLocation(latitude, longitude);

        // Then
        assertThat(property.getLocation()).isNotNull();
        assertThat(property.getLocation().getLat()).isEqualTo(latitude);
        assertThat(property.getLocation().getLon()).isEqualTo(longitude);
    }

    @Test
    void setLocation_ShouldHandleNullLatitude() {
        // Given
        PropertyDocument property = new PropertyDocument();

        // When
        property.setLocation(null, 103.8198);

        // Then
        assertThat(property.getLocation()).isNull();
    }

    @Test
    void setLocation_ShouldHandleNullLongitude() {
        // Given
        PropertyDocument property = new PropertyDocument();

        // When
        property.setLocation(1.3521, null);

        // Then
        assertThat(property.getLocation()).isNull();
    }

    @Test
    void setLocation_ShouldHandleBothNullValues() {
        // Given
        PropertyDocument property = new PropertyDocument();

        // When
        property.setLocation(null, null);

        // Then
        assertThat(property.getLocation()).isNull();
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        PropertyDocument property1 = PropertyDocument.builder()
                .id("1")
                .title("Modern Apartment")
                .price(new BigDecimal("3500.00"))
                .lastUpdated(now)
                .build();

        PropertyDocument property2 = PropertyDocument.builder()
                .id("1")
                .title("Modern Apartment")
                .price(new BigDecimal("3500.00"))
                .lastUpdated(now)
                .build();

        PropertyDocument property3 = PropertyDocument.builder()
                .id("2")
                .title("Different Apartment")
                .price(new BigDecimal("4000.00"))
                .lastUpdated(now)
                .build();

        // Then
        assertThat(property1).isEqualTo(property2);
        assertThat(property1).isNotEqualTo(property3);
        assertThat(property1).isNotEqualTo(null);
    }

    @Test
    void toString_ShouldContainRelevantInformation() {
        // Given
        PropertyDocument property = PropertyDocument.builder()
                .id("1")
                .title("Modern Apartment")
                .price(new BigDecimal("3500.00"))
                .city("Singapore")
                .build();

        // When
        String toString = property.toString();

        // Then
        assertThat(toString).contains("PropertyDocument");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("title=Modern Apartment");
        assertThat(toString).contains("price=3500.00");
        assertThat(toString).contains("city=Singapore");
    }
}