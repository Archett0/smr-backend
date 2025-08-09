package com.team12.useractionservice.repository;

import com.team12.useractionservice.model.UserAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserActionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserActionRepository userActionRepository;

    @Test
    void findByUserIdAndActionValue_shouldReturnActions() {
        entityManager.clear();
        // Given
        Long userId = 100L;
        int actionValue = 1;
        UserAction action1 = new UserAction(userId, 101L, actionValue);
        UserAction action2 = new UserAction(userId, 102L, actionValue);
        UserAction action3 = new UserAction(2L, 101L, actionValue); // 不同用户

        entityManager.persist(action1);
        entityManager.persist(action2);
        entityManager.persist(action3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<UserAction> result = userActionRepository.findByUserIdAndActionValue(userId, actionValue);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ua -> ua.getUserId().equals(userId)));
        assertTrue(result.stream().allMatch(ua -> ua.getActionValue() == actionValue));
    }

    @Test
    void findByUserIdAndListingId_shouldReturnAction() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        UserAction action = new UserAction(userId, listingId, 1);
        entityManager.persist(action);
        entityManager.flush();

        // When
        UserAction result = userActionRepository.findByUserIdAndListingId(userId, listingId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(listingId, result.getListingId());
    }

    @Test
    void findUserIdsByListingIdAndFavorited_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        UserAction action1 = new UserAction(1L, listingId, 1); // 收藏
        UserAction action2 = new UserAction(2L, listingId, 1); // 收藏
        UserAction action3 = new UserAction(3L, listingId, 2); // 不是收藏

        entityManager.persist(action1);
        entityManager.persist(action2);
        entityManager.persist(action3);
        entityManager.flush();

        // When
        List<Long> result = userActionRepository.findUserIdsByListingIdAndFavorited(listingId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertFalse(result.contains(3L));
    }

    @Test
    void existsByUserIdAndListingIdAndFavorited_shouldReturnTrueWhenExists() {
        // Given
        String userId = "1";
        Long listingId = 101L;
        UserAction action = new UserAction(Long.parseLong(userId), listingId, 1);
        entityManager.persist(action);
        entityManager.flush();

        // When
        boolean exists = userActionRepository.existsByUserIdAndListingIdAndFavorited(userId, listingId);

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByUserIdAndListingIdAndFavorited_shouldReturnFalseWhenNotExists() {
        // Given - 不创建任何记录

        // When
        boolean exists = userActionRepository.existsByUserIdAndListingIdAndFavorited("1", 101L);

        // Then
        assertFalse(exists);
    }

    @Test
    void findByUserIdAndListingIdAndActionValue_shouldReturnAction() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        int actionValue = 1;
        UserAction action = new UserAction(userId, listingId, actionValue);
        entityManager.persist(action);
        entityManager.flush();

        // When
        Optional<UserAction> result = userActionRepository.findByUserIdAndListingIdAndActionValue(
                userId, listingId, actionValue);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals(listingId, result.get().getListingId());
        assertEquals(actionValue, result.get().getActionValue());
    }

    @Test
    void deleteByUserIdAndListingIdAndActionValue_shouldDeleteAction() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        int actionValue = 1;
        UserAction action = new UserAction(userId, listingId, actionValue);
        entityManager.persist(action);
        entityManager.flush();

        // When
        userActionRepository.deleteByUserIdAndListingIdAndActionValue(userId, listingId, actionValue);

        // Then
        assertNull(entityManager.find(UserAction.class, action.getId()));
    }

    @Test
    void findUserIdsByListingIdAndPriceAlert_shouldReturnUserIds() {
        // Given
        Long listingId = 101L;
        UserAction action1 = new UserAction(1L, listingId, 2); // 价格提醒
        UserAction action2 = new UserAction(2L, listingId, 2); // 价格提醒
        UserAction action3 = new UserAction(3L, listingId, 1); // 不是价格提醒

        entityManager.persist(action1);
        entityManager.persist(action2);
        entityManager.persist(action3);
        entityManager.flush();

        // When
        List<Long> result = userActionRepository.findUserIdsByListingIdAndPriceAlert(listingId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertFalse(result.contains(3L));
    }

    @Test
    void existsByUserIdAndListingIdAndActionValue_shouldReturnCorrectBoolean() {
        // Given
        Long userId = 1L;
        Long listingId = 101L;
        int actionValue = 1;
        UserAction action = new UserAction(userId, listingId, actionValue);
        entityManager.persist(action);
        entityManager.flush();

        // When - 存在的情况
        boolean exists = userActionRepository.existsByUserIdAndListingIdAndActionValue(
                userId, listingId, actionValue);

        // Then
        assertTrue(exists);

        // When - 不存在的情况
        boolean notExists = userActionRepository.existsByUserIdAndListingIdAndActionValue(
                userId, listingId, 2); // 不同的actionValue

        // Then
        assertFalse(notExists);
    }
}
