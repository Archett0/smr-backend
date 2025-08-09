package com.team12.useractionservice.service;

import com.team12.useractionservice.dto.UserActionDto;
import com.team12.useractionservice.model.UserAction;
import com.team12.useractionservice.model.UserActionType;
import com.team12.useractionservice.repository.UserActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserActionServiceTest {

    @Mock
    private UserActionRepository repository;

    @InjectMocks
    private UserActionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private UserActionDto createActionDto(Long id, Long userId, Long listingId, int actionValue) {
        return new UserActionDto(id, userId, listingId, actionValue);
    }

    private UserAction createAction(Long userId, Long listingId, int actionValue) {
        UserAction action = new UserAction(userId, listingId, actionValue);
        action.setCreatedAt(LocalDateTime.now());
        return action;
    }

    @Test
    void trackAction_shouldCreateNewWhenNotExists() {
        UserActionDto dto = createActionDto(1L, 1L, 101L, 1);
        when(repository.findByUserIdAndListingId(1L, 101L)).thenReturn(null);
        when(repository.save(any(UserAction.class))).thenAnswer(inv -> inv.getArgument(0));

        UserAction result = service.trackAction(dto);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(repository).save(any(UserAction.class));
    }

    @Test
    void trackAction_shouldUpdateWhenExistsWithDifferentValue() {
        UserAction existing = createAction(1L, 101L, 1);
        UserActionDto dto = createActionDto(1L, 1L, 101L, -1);

        when(repository.findByUserIdAndListingId(1L, 101L)).thenReturn(existing);
        when(repository.save(any(UserAction.class))).thenAnswer(inv -> inv.getArgument(0));

        UserAction result = service.trackAction(dto);

        assertEquals(-1, result.getActionValue());
    }

    @Test
    void getUserFavorites_shouldReturnFavorites() {
        List<UserAction> actions = Arrays.asList(
                createAction(1L, 101L, 1),
                createAction(1L, 102L, 1)
        );
        when(repository.findByUserIdAndActionValue(1L, 1)).thenReturn(actions);

        List<UserAction> result = service.getUserFavorites(1L);

        assertEquals(2, result.size());
        verify(repository).findByUserIdAndActionValue(1L, 1);
    }

    @Test
    void alertPriceAction_shouldCreateNewAlert() {
        UserActionDto dto = createActionDto(1L, 1L, 101L, 2);
        when(repository.findByUserIdAndListingIdAndActionValue(1L, 101L, 2))
                .thenReturn(Optional.empty());
        when(repository.save(any(UserAction.class))).thenAnswer(inv -> inv.getArgument(0));

        UserAction result = service.alertPriceAction(dto);

        assertNotNull(result);
        assertEquals(2, result.getActionValue());
    }

    @Test
    void alertPriceAction_shouldDeleteAlert() {
        UserActionDto dto = createActionDto(1L, 1L, 101L, -2);
        service.alertPriceAction(dto);

        verify(repository).deleteByUserIdAndListingIdAndActionValue(1L, 101L, 2);
    }

    @Test
    void getUserIdsWhoFavoritedListing_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        List<Long> expectedUserIds = Arrays.asList(1L, 2L, 3L);
        when(repository.findUserIdsByListingIdAndFavorited(listingId)).thenReturn(expectedUserIds);

        // When
        List<Long> result = service.getUserIdsWhoFavoritedListing(listingId);

        // Then
        assertEquals(3, result.size());
        assertEquals(expectedUserIds, result);
        verify(repository).findUserIdsByListingIdAndFavorited(listingId);
    }

    @Test
    void getUserIdsWhoFavoritedListing_shouldThrowForInvalidListingId() {
        // Given - 无效的listingId
        Long invalidListingId = -1L;

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserIdsWhoFavoritedListing(invalidListingId));
        verify(repository, never()).findUserIdsByListingIdAndFavorited(any());
    }

    @Test
    void isListingFavoritedByUser_shouldReturnTrueWhenFavorited() {
        // Given
        String userId = "1";
        Long listingId = 101L;
        when(repository.existsByUserIdAndListingIdAndFavorited(userId, listingId)).thenReturn(true);

        // When
        boolean result = service.isListingFavoritedByUser(userId, listingId);

        // Then
        assertTrue(result);
        verify(repository).existsByUserIdAndListingIdAndFavorited(userId, listingId);
    }

    @Test
    void isListingFavoritedByUser_shouldReturnFalseWhenNotFavorited() {
        // Given
        String userId = "1";
        Long listingId = 101L;
        when(repository.existsByUserIdAndListingIdAndFavorited(userId, listingId)).thenReturn(false);

        // When
        boolean result = service.isListingFavoritedByUser(userId, listingId);

        // Then
        assertFalse(result);
    }

    @Test
    void isListingFavoritedByUser_shouldReturnFalseForNullInput() {
        // When & Then
        assertFalse(service.isListingFavoritedByUser(null, 101L));
        assertFalse(service.isListingFavoritedByUser("1", null));
        verify(repository, never()).existsByUserIdAndListingIdAndFavorited(any(), any());
    }

    @Test
    void getUsersWithPriceAlert_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        List<Long> expectedUserIds = Arrays.asList(1L, 2L);
        when(repository.findUserIdsByListingIdAndPriceAlert(listingId)).thenReturn(expectedUserIds);

        // When
        List<Long> result = service.getUsersWithPriceAlert(listingId);

        // Then
        assertEquals(2, result.size());
        assertEquals(expectedUserIds, result);
        verify(repository).findUserIdsByListingIdAndPriceAlert(listingId);
    }

    @Test
    void isPriceAlertEnabled_shouldReturnTrueWhenEnabled() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        when(repository.existsByUserIdAndListingIdAndActionValue(
                userId, listingId, UserActionType.PRICE_ALERT.getValue()))
                .thenReturn(true);

        // When
        boolean result = service.isPriceAlertEnabled(userId, listingId);

        // Then
        assertTrue(result);
        verify(repository).existsByUserIdAndListingIdAndActionValue(
                userId, listingId, UserActionType.PRICE_ALERT.getValue());
    }

    @Test
    void isPriceAlertEnabled_shouldReturnFalseWhenNotEnabled() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        when(repository.existsByUserIdAndListingIdAndActionValue(
                userId, listingId, UserActionType.PRICE_ALERT.getValue()))
                .thenReturn(false);

        // When
        boolean result = service.isPriceAlertEnabled(userId, listingId);

        // Then
        assertFalse(result);
    }
}