package com.team12.useractionservice.repository;

import com.team12.useractionservice.model.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    // 查询用户对某个房源的最新动作
    UserAction findTopByUserIdAndListingIdOrderByCreatedAtDesc(Long userId, Long listingId);

    // 查询用户的所有收藏房源
    List<UserAction> findByUserIdAndActionValue(Long userId, int actionValue);

    UserAction findByUserIdAndListingId(Long userId, Long listingId);

    // 查询收藏指定房源的所有用户ID（actionValue = 1）
    @Query("SELECT DISTINCT ua.userId FROM UserAction ua " +
            "WHERE ua.listingId = :listingId AND ua.actionValue = 1")
    List<Long> findUserIdsByListingIdAndFavorited(@Param("listingId") Long listingId);
}
