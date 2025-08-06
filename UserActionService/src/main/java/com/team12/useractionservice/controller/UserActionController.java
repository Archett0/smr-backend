package com.team12.useractionservice.controller;

import com.team12.useractionservice.dto.UserActionDto;
import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.service.UserActionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/actions")
@Tag(name = "UserAction Controller APIs", description = "CRUD for actions")
public class UserActionController {

    private final UserActionService service;

    public UserActionController(UserActionService service) {
        this.service = service;
    }

    // 收藏/取消收藏
    @PostMapping
    public ResponseEntity<UserAction> trackAction(@RequestBody UserActionDto dto) {
        UserAction action = service.trackAction(dto);
        return ResponseEntity.ok(action);
    }

    // 获取用户收藏列表
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<UserAction>> getFavorites(@PathVariable Long userId) {
        List<UserAction> favorites = service.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/favorites/listing/{listingId}")
    public ResponseEntity<List<Long>> getUserIdsByFavoritedListing(
            @PathVariable Long listingId
    ) {
        List<Long> userIds = service.getUserIdsWhoFavoritedListing(listingId);
        return ResponseEntity.ok(userIds);
    }
}
