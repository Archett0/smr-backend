package com.team12.listingservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String img;

    @Embedded
    private GeoLocation location;

    @Column(nullable = false)
    private int numBedrooms;

    @Column(nullable = false)
    private int numBathrooms;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false, updatable = false)
    private LocalDateTime postedAt;

    @Column(nullable = false, length = 64)
    private String agentId;
}
