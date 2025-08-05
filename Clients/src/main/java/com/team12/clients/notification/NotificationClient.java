package com.team12.clients.notification;

import com.team12.clients.FeignTokenRelayConfig;
import com.team12.clients.notification.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification", path = "/notification", configuration = FeignTokenRelayConfig.class)
public interface NotificationClient {

    @PostMapping("/send")
    void sendNotification(@RequestBody NotificationRequest notificationRequest);
}
