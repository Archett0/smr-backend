package com.team12.clients.listing;

import com.team12.clients.FeignTokenRelayConfig;
import com.team12.clients.listing.dto.PropertyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "listing", path = "/listing", configuration = FeignTokenRelayConfig.class)
public interface ListingClient {
    @GetMapping("/{id}")
    PropertyDto getPropertyById(@PathVariable("id") Long id);

    @GetMapping("")
    List<PropertyDto> getAllProperties();
}
