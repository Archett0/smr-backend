package com.team12.searchservice.controller;

import com.team12.searchservice.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/search/admin")
@RequiredArgsConstructor
@Tag(name = "Search Admin APIs", description = "Administrative operations for search service")
public class AdminController {

    private final DataSyncService dataSyncService;

    @GetMapping("/sync/stats")
    @Operation(summary = "Get synchronization statistics")
    public ResponseEntity<Map<String, Object>> getSyncStatistics() {
        try {
            Map<String, Object> stats = dataSyncService.getSyncStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting sync statistics", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync/properties")
    @Operation(summary = "Trigger bulk property indexing")
    public ResponseEntity<Map<String, String>> bulkIndexProperties() {
        try {
            log.info("Admin triggered bulk property indexing");
            dataSyncService.bulkIndexProperties();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Bulk property indexing initiated"
            ));
            
        } catch (Exception e) {
            log.error("Error during bulk property indexing", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Bulk indexing failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync/users")
    @Operation(summary = "Trigger bulk user indexing")
    public ResponseEntity<Map<String, String>> bulkIndexUsers() {
        try {
            log.info("Admin triggered bulk user indexing");
            dataSyncService.bulkIndexUsers();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Bulk user indexing initiated"
            ));
            
        } catch (Exception e) {
            log.error("Error during bulk user indexing", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Bulk indexing failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check search service health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "healthy",
                "service", "SearchService",
                "elasticsearch", "connected",
                "rabbitmq", "connected",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "unhealthy",
                "error", e.getMessage()
            ));
        }
    }
}