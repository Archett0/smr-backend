package com.team12.recommendationservice.controller;

import com.team12.recommendationservice.model.Property;
import com.team12.recommendationservice.service.RecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendation")
@Tag(name = "Recommendation Controller APIs", description = "CRUD for recommendation")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public List<Property> getRecommendations(@PathVariable(required = false) Long userId) {
        return service.recommendListings(userId);
    }
}
