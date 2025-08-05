package com.team12.recommendationservice.feignclient;

import com.team12.recommendationservice.model.Property;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "listing-service", url = "${feign.client.listing-service.url}")  // 从配置读取URL
public interface ListingClient {

    @GetMapping("/api/listings/available")
    List<Property> getAllAvailableListings();

    @GetMapping("/api/listings/{id}")
    Property getListingById(@PathVariable Long id);
}