package com.team12.listingservice.controller;

import com.team12.listingservice.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/listing/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller APIs", description = "Administrative operations for properties")
public class AdminController {

    private final PropertyService propertyService;

    @PostMapping("/sync/bulk")
    @Operation(summary = "Bulk sync all properties to Elasticsearch")
    public ResponseEntity<Map<String, String>> bulkSyncProperties() {
        try {
            log.info("Admin triggered bulk property sync");
            propertyService.bulkSyncAllProperties();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Bulk sync initiated successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error during bulk sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Bulk sync failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get property statistics")
    public ResponseEntity<Map<String, Object>> getPropertyStatistics() {
        try {
            Map<String, Object> stats = propertyService.getPropertyStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting property statistics", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync/property/{id}")
    @Operation(summary = "Manually sync a specific property to Elasticsearch")
    public ResponseEntity<Map<String, String>> syncSpecificProperty(@PathVariable Long id) {
        try {
            log.info("Admin triggered manual sync for property: {}", id);
            
            var propertyDto = propertyService.getPropertyById(id);
            if (propertyDto == null || propertyDto.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Trigger update sync for the property
            propertyService.updateProperty(id, propertyDto.get().getProperty());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Property " + id + " synced successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error syncing property: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Sync failed: " + e.getMessage()
            ));
        }
    }
}