package com.team12.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class IdentityVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String agentAuth0Id;

    private String reason;
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
