package com.team12.clients.user;

import com.team12.clients.FeignTokenRelayConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user", path = "/auth", configuration = FeignTokenRelayConfig.class)
public interface UserClient {

    @GetMapping("/getAgentInfo/{id}")
    ResponseEntity<List<String>> getAgentInfoById(@PathVariable("id") Long id);
}
