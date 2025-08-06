package com.team12.useractionservice.repository;

import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.model.UserActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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


    Optional<UserAction> findByUserIdAndListingIdAndActionValue(
            Long userId, Long listingId, int actionValue);

    void deleteByUserIdAndListingIdAndActionValue(
            Long userId, Long listingId, int actionValue);

    @Query("SELECT ua.userId FROM UserAction ua " +
            " WHERE ua.listingId = :listingId AND ua.actionValue = 2")
    List<Long> findUserIdsByListingIdAndPriceAlert(@Param("listingId") Long listingId);

    boolean existsByUserIdAndListingIdAndActionValue(
            Long userId,
            Long listingId,
            int actionValue
    );
}
