package com.team12.useractionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long listingId;  // 房源ID

    @Column(nullable = false)
    private int actionValue; // 1或-1

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt  = LocalDateTime.now();

    public UserAction(Long userId, Long listingId, int actionValue) {
        this.userId = userId;
        this.listingId = listingId;
        this.actionValue = actionValue;
    }
}
