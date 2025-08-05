package com.team12.recommendationservice.service;

import com.team12.recommendationservice.feignclient.ListingClient;
import com.team12.recommendationservice.feignclient.UserActionClient;
import com.team12.recommendationservice.model.GeoLocation;
import com.team12.recommendationservice.model.Property;
import com.team12.recommendationservice.model.UserAction;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    private  ListingClient listingClient;
    private  UserActionClient userActionClient;

    public List<Property> recommendListings(Long userId) {
        // 1. 获取所有可租房源
        List<Property> allListings = listingClient.getAllAvailableListings();

        // 2. 尝试获取用户收藏（若无则返回空列表）
        List<UserAction> favorites = userId != null ?
                userActionClient.getUserFavorites(userId) :
                Collections.emptyList();

        // 3. 计算推荐分数
        Map<Property, Double> scoredListings = new HashMap<>();
        for (Property listing : allListings) {
            double score = calculateScore(listing, favorites);
            scoredListings.put(listing, score);
        }

        // 4. 按分数排序并返回Top 15
        return scoredListings.entrySet().stream()
                .sorted(Map.Entry.<Property, Double>comparingByValue().reversed())
                .limit(15)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateScore(Property listing, List<UserAction> favorites) {
        double score = 0.0;

        // 基础分：新上架房源加分
        score += 1.0 / (ChronoUnit.DAYS.between(listing.getPostedAt(), LocalDateTime.now()) + 1);

        // 个性化加分（如果有收藏记录）
        if (!favorites.isEmpty()) {
            // 获取用户偏好（根据收藏记录）
            Property favorite = listingClient.getListingById(favorites.get(0).listingId);

            // 价格匹配（±20%区间内满分，否则线性递减）
            double priceRatio = Math.abs(listing.getPrice().doubleValue() / favorite.getPrice().doubleValue() - 1);
            score += priceRatio <= 0.2 ? 2.0 : 2.0 - priceRatio;

            // 户型匹配
            if (listing.getNumBedrooms() == favorite.getNumBedrooms()) score += 1.5;
            if (listing.getNumBathrooms() == favorite.getNumBathrooms()) score += 1.0;

            // 距离匹配（如果有坐标）
            if (listing.getLocation() != null && favorite.getLocation() != null) {
                double distance = calculateDistance(
                        listing.getLocation(),
                        favorite.getLocation()
                );
                score += 1.0 / (distance + 1); // 距离越近分越高
            }
        }

        return score;
    }

    // 距离计算
    private double calculateDistance(GeoLocation loc1, GeoLocation loc2) {
        double lat1 = loc1.latitude;
        double lon1 = loc1.longitude;
        double lat2 = loc2.latitude;
        double lon2 = loc2.longitude;

        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }
}
