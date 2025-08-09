package com.team12.useractionservice.controller;

import com.team12.clients.listing.ListingClient;
import com.team12.clients.listing.dto.PropertyDto;
import com.team12.clients.listing.dto.Property;
import com.team12.useractionservice.dto.UserActionDto;
import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.service.UserActionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/actions")
@Tag(name = "UserAction Controller APIs", description = "CRUD for actions")
public class UserActionController {

    private final UserActionService service;
    private final ListingClient listingClient;

    public UserActionController(UserActionService service, ListingClient listingClient) {
        this.service = service;
        this.listingClient = listingClient;
    }

    // 收藏/取消收藏
    @PostMapping
    public ResponseEntity<UserAction> trackAction(@RequestBody UserActionDto dto) {
        UserAction action = service.trackAction(dto);
        return ResponseEntity.ok(action);
    }

    // 获取用户收藏列表
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<Property>> getFavoriteProperties(@PathVariable Long userId) {
        // 1. 获取用户收藏的房源ID列表
        List<Long> favoriteListingIds = service.getUserFavorites(userId)
                .stream()
                .map(UserAction::getListingId)
                .toList();

        // 2. 获取完整房源信息（提取嵌套的Property对象）
        List<Property> properties = favoriteListingIds.stream()
                .map(listingClient::getPropertyById) // 调用Feign客户端
                .filter(Objects::nonNull)
                .map(PropertyDto::getProperty)       // 从PropertyDto中提取Property
                .filter(Objects::nonNull)            // 二次过滤
                .toList();

        return ResponseEntity.ok(properties);
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Property>> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "15") int limit) {

        // 1. 获取用户已收藏的房源ID（用于过滤）
        Set<Long> favoriteIds = service.getUserFavorites(userId)
                .stream()
                .map(UserAction::getListingId)
                .collect(Collectors.toSet());

        // 2. 从listing-service获取所有可用房源
        List<PropertyDto> allListings = listingClient.getAllProperties();

        // 3. 过滤+随机推荐
        List<Property> recommendations = allListings.stream()
                .map(PropertyDto::getProperty)
                .filter(p -> !favoriteIds.contains(p.getId())) // 排除已收藏
                .sorted(Comparator.comparingDouble(p -> Math.random())) // 随机排序
                .limit(limit) // 限制数量
                .toList();

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/favorites/listing/{listingId}")
    public ResponseEntity<List<Long>> getUserIdsByFavoritedListing(
            @PathVariable Long listingId
    ) {
        List<Long> userIds = service.getUserIdsWhoFavoritedListing(listingId);
        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/favorites/check")
    public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(
            @RequestParam String userId,
            @RequestParam Long listingId
    ) {
        boolean isFavorited = service.isListingFavoritedByUser(userId, listingId);
        return ResponseEntity.ok(Collections.singletonMap("isFavorited", isFavorited));
    }

    // 收藏/取消收藏
    @PostMapping("/price-alert")
    public ResponseEntity<UserAction> alertPriceAction(@RequestBody UserActionDto dto) {
        UserAction action = service.alertPriceAction(dto);
        return ResponseEntity.ok(action);
    }


    @GetMapping("/{propertyId}/price-alert-users")
    public ResponseEntity<List<Long>> getPriceAlertUsers(
            @PathVariable("propertyId") Long propertyId
    ) {
        List<Long> userIds = service.getUsersWithPriceAlert(propertyId);
        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/{propertyId}/price-alert/{userId}")
    public ResponseEntity<Boolean> isPriceAlertEnabled(
            @PathVariable("propertyId") Long propertyId,
            @PathVariable("userId") Long userId
    ) {
        boolean enabled = service.isPriceAlertEnabled(userId, propertyId);
        return ResponseEntity.ok(enabled);
    }
}
