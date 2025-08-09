package com.team12.useractionservice.service;

import com.team12.useractionservice.dto.UserActionDto;
import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.model.UserActionType;
import com.team12.useractionservice.repository.UserActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionServiceTest {

    @Mock
    private UserActionRepository repository;

    @InjectMocks
    private UserActionService service;

    @Test
    void trackAction_shouldCreateNewActionWhenNoExistingRecord() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.FAVORITE.getValue());
        when(repository.findByUserIdAndListingId(1L, 101L)).thenReturn(null);
        when(repository.save(any(UserAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserAction result = service.trackAction(dto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(101L, result.getListingId());
        assertEquals(UserActionType.FAVORITE.getValue(), result.getActionValue());
        verify(repository, times(1)).save(any(UserAction.class));
    }

    @Test
    void trackAction_shouldUpdateActionWhenExistingRecordWithDifferentValue() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.UNFAVORITE.getValue());
        UserAction existing = new UserAction(1L, 101L, UserActionType.FAVORITE.getValue());
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(repository.findByUserIdAndListingId(1L, 101L)).thenReturn(existing);
        when(repository.save(any(UserAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserAction result = service.trackAction(dto);

        // Then
        assertNotNull(result);
        assertEquals(UserActionType.UNFAVORITE.getValue(), result.getActionValue());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void trackAction_shouldReturnExistingWhenSameActionValue() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.FAVORITE.getValue());
        UserAction existing = new UserAction(1L, 101L, UserActionType.FAVORITE.getValue());

        when(repository.findByUserIdAndListingId(1L, 101L)).thenReturn(existing);

        // When
        UserAction result = service.trackAction(dto);

        // Then
        assertSame(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void trackAction_shouldThrowExceptionForInvalidActionValue() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, 99); // 无效值

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.trackAction(dto));
    }

    @Test
    void getUserFavorites_shouldReturnFavoriteActions() {
        // Given
        Long userId = 1L;
        List<UserAction> expected = Arrays.asList(
                new UserAction(userId, 101L, UserActionType.FAVORITE.getValue()),
                new UserAction(userId, 102L, UserActionType.FAVORITE.getValue())
        );
        when(repository.findByUserIdAndActionValue(userId, UserActionType.FAVORITE.getValue()))
                .thenReturn(expected);

        // When
        List<UserAction> result = service.getUserFavorites(userId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getActionValue() == UserActionType.FAVORITE.getValue()));
    }

    @Test
    void getUserIdsWhoFavoritedListing_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        List<Long> expected = Arrays.asList(1L, 2L, 3L);
        when(repository.findUserIdsByListingIdAndFavorited(listingId)).thenReturn(expected);

        // When
        List<Long> result = service.getUserIdsWhoFavoritedListing(listingId);

        // Then
        assertEquals(3, result.size());
        assertEquals(expected, result);
    }

    @Test
    void getUserIdsWhoFavoritedListing_shouldThrowExceptionForInvalidListingId() {
        assertThrows(IllegalArgumentException.class, () -> service.getUserIdsWhoFavoritedListing(null));
        assertThrows(IllegalArgumentException.class, () -> service.getUserIdsWhoFavoritedListing(0L));
        assertThrows(IllegalArgumentException.class, () -> service.getUserIdsWhoFavoritedListing(-1L));
    }

    @Test
    void isListingFavoritedByUser_shouldReturnTrueWhenFavorited() {
        // Given
        when(repository.existsByUserIdAndListingIdAndFavorited("1", 101L)).thenReturn(true);

        // When
        boolean result = service.isListingFavoritedByUser("1", 101L);

        // Then
        assertTrue(result);
    }

    @Test
    void isListingFavoritedByUser_shouldReturnFalseForNullInput() {
        assertFalse(service.isListingFavoritedByUser(null, 101L));
        assertFalse(service.isListingFavoritedByUser("1", null));
    }

    @Test
    void alertPriceAction_shouldCreateNewAlertWhenNotExists() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.PRICE_ALERT.getValue());
        when(repository.findByUserIdAndListingIdAndActionValue(1L, 101L, UserActionType.PRICE_ALERT.getValue()))
                .thenReturn(Optional.empty());
        when(repository.save(any(UserAction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserAction result = service.alertPriceAction(dto);

        // Then
        assertNotNull(result);
        assertEquals(UserActionType.PRICE_ALERT.getValue(), result.getActionValue());
        verify(repository, times(1)).save(any(UserAction.class));
    }

    @Test
    void alertPriceAction_shouldReturnExistingAlertWhenExists() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.PRICE_ALERT.getValue());
        UserAction existing = new UserAction(1L, 101L, UserActionType.PRICE_ALERT.getValue());
        when(repository.findByUserIdAndListingIdAndActionValue(1L, 101L, UserActionType.PRICE_ALERT.getValue()))
                .thenReturn(Optional.of(existing));

        // When
        UserAction result = service.alertPriceAction(dto);

        // Then
        assertSame(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void alertPriceAction_shouldDeleteAlertForCancelAction() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, UserActionType.CANCEL_PRICE_ALERT.getValue());

        // When
        UserAction result = service.alertPriceAction(dto);

        // Then
        assertNull(result);
        verify(repository, times(1)).deleteByUserIdAndListingIdAndActionValue(
                1L, 101L, UserActionType.PRICE_ALERT.getValue());
    }

    @Test
    void alertPriceAction_shouldThrowExceptionForInvalidActionValue() {
        // Given
        UserActionDto dto = new UserActionDto(1L, 1L, 101L, 99); // 无效值

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.alertPriceAction(dto));
    }

    @Test
    void getUsersWithPriceAlert_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        List<Long> expected = Arrays.asList(1L, 2L);
        when(repository.findUserIdsByListingIdAndPriceAlert(listingId)).thenReturn(expected);

        // When
        List<Long> result = service.getUsersWithPriceAlert(listingId);

        // Then
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }

    @Test
    void isPriceAlertEnabled_shouldReturnCorrectStatus() {
        // Given
        when(repository.existsByUserIdAndListingIdAndActionValue(1L, 101L, UserActionType.PRICE_ALERT.getValue()))
                .thenReturn(true);

        // When
        boolean result = service.isPriceAlertEnabled(1L, 101L);

        // Then
        assertTrue(result);
    }
}
