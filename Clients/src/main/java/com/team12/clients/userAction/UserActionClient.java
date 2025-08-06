package com.team12.clients.userAction;

import com.team12.clients.FeignTokenRelayConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-action", path = "/actions", configuration = FeignTokenRelayConfig.class)
public interface UserActionClient {

    @GetMapping("/{propertyId}/price-alert-users")
    List<Long> getPriceAlertUsers(@PathVariable("propertyId") Long propertyId);
}
