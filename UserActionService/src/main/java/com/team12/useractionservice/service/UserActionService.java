package com.team12.useractionservice.service;

import com.team12.useractionservice.dto.UserActionDto;
import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.model.UserActionType;
import com.team12.useractionservice.repository.UserActionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserActionService {

    private final UserActionRepository repository;

    public UserAction trackAction(UserActionDto dto) {
        // 1. 验证动作值合法性
        validateActionValue(dto.getActionValue());

        // 2. 查询现有记录
        UserAction existingAction = repository.findByUserIdAndListingId(dto.getUserId(), dto.getListingId());

        // 3. 处理逻辑
        if (existingAction == null) {
            // 无记录 → 直接创建
            return createNewAction(dto);
        } else if (existingAction.getActionValue() != dto.getActionValue()) {
            // 有记录且动作值不同 → 更新
            return updateAction(existingAction, dto.getActionValue());
        } else {
            // 有记录且动作值相同 → 无操作
            return existingAction;
        }
    }

    // 辅助方法：验证动作值
    private void validateActionValue(int actionValue) {
        if (actionValue != UserActionType.FAVORITE.getValue() &&
                actionValue != UserActionType.UNFAVORITE.getValue()) {
            throw new IllegalArgumentException("Invalid action value. Use 1 (FAVORITE) or -1 (UNFAVORITE)");
        }
    }

    // 辅助方法：创建新记录
    private UserAction createNewAction(UserActionDto dto) {
        UserAction action = new UserAction(
                dto.getUserId(),
                dto.getListingId(),
                dto.getActionValue()
        );
        return repository.save(action);
    }

    // 辅助方法：更新记录
    private UserAction updateAction(UserAction existingAction, int newActionValue) {
        existingAction.setActionValue(newActionValue);
        existingAction.setCreatedAt(LocalDateTime.now()); // 更新时间戳
        return repository.save(existingAction);
    }

    public List<UserAction> getUserFavorites(Long userId) {
        return repository.findByUserIdAndActionValue(
                userId,
                UserActionType.FAVORITE.getValue()
        );
    }

    public List<Long> getUserIdsWhoFavoritedListing(Long listingId) {
        // 验证listingId非空
        if (listingId == null || listingId <= 0) {
            throw new IllegalArgumentException("Invalid listingId");
        }

        // 查询并返回用户ID列表
        return repository.findUserIdsByListingIdAndFavorited(listingId);
    }

    public boolean isListingFavoritedByUser(String userId, Long listingId) {
        if (userId == null || listingId == null) {
            return false;
        }
        return repository.existsByUserIdAndListingIdAndFavorited(userId, listingId);
    }

    @Transactional
    public UserAction alertPriceAction(UserActionDto dto) {
        if (dto.getActionValue() == UserActionType.PRICE_ALERT.getValue()) {
            return repository
                    .findByUserIdAndListingIdAndActionValue(dto.getUserId(), dto.getListingId(), UserActionType.PRICE_ALERT.getValue())
                    .orElseGet(() -> {
                        UserAction ua = UserAction.builder()
                                .userId(dto.getUserId())
                                .listingId(dto.getListingId())
                                .actionValue(2)
                                .build();
                        return repository.save(ua);
                    });
        }
        else if (dto.getActionValue() == UserActionType.CANCEL_PRICE_ALERT.getValue()) {
            repository.deleteByUserIdAndListingIdAndActionValue(
                    dto.getUserId(),
                    dto.getListingId(),
                    UserActionType.PRICE_ALERT.getValue()
            );
            return null;
        }
        else {
            throw new IllegalArgumentException("Invalid actionValue for price alert: " + dto.getActionValue());
        }
    }

    public List<Long> getUsersWithPriceAlert(Long listingId) {
        return repository.findUserIdsByListingIdAndPriceAlert(listingId);
    }

    public boolean isPriceAlertEnabled(Long userId, Long listingId) {
        int priceAlertValue = UserActionType.PRICE_ALERT.getValue();
        return repository.existsByUserIdAndListingIdAndActionValue(
                userId, listingId, priceAlertValue
        );
    }
}
