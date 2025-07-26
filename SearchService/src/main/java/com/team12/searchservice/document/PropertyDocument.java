package com.team12.searchservice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "properties")
@Setting(shards = 1, replicas = 0)
public class PropertyDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String address;

    @Field(type = FieldType.Keyword)
    private String img;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Integer)
    private Integer numBedrooms;

    @Field(type = FieldType.Integer)
    private Integer numBathrooms;

    @Field(type = FieldType.Boolean)
    private Boolean available;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime postedAt;

    @Field(type = FieldType.Keyword)
    private String agentId;

    // Additional search fields
    @Field(type = FieldType.Keyword)
    private String propertyType;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String district;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime lastUpdated;

    // Convenience method to create GeoPoint from latitude and longitude
    public void setLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            this.location = new GeoPoint(latitude, longitude);
        }
    }
} 