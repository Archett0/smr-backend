package com.team12.recommendationservice.feignclient;

import com.team12.recommendationservice.model.UserAction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-action-service", url = "${feign.client.user-action-service.url}")
public interface UserActionClient {
    @GetMapping("/actions/favorites/{userId}")
    List<UserAction> getUserFavorites(@PathVariable Long userId);
}