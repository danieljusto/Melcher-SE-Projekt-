package com.group_2.model.cleaning;

import com.group_2.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RoomAssignmentQueue.
 * Plain JUnit tests with no Spring context as recommended for utility classes.
 * Uses real User instances instead of mocks for Java 25+ compatibility.
 */
class RoomAssignmentQueueTest {

    private RoomAssignmentQueue queue;
    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        queue = new RoomAssignmentQueue();

        // Create real User instances with IDs set via reflection
        user1 = createUserWithId(1L, "User1");
        user2 = createUserWithId(2L, "User2");
        user3 = createUserWithId(3L, "User3");
    }

    /**
     * Helper method to create a User with a specific ID.
     * Uses reflection since ID is normally set by JPA.
     */
    private User createUserWithId(Long id, String name) {
        User user = new User(name);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID via reflection", e);
        }
        return user;
    }

    @Test
    void initializesQueueWithMembers() {
        // When
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);

        // Then
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void initializesQueueWithOffset() {
        // When - offset 1 means start with user2
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 1);

        // Then - order should be [2, 3, 1]
        assertThat(queue.getMemberIds()).containsExactly(2L, 3L, 1L);
    }

    @Test
    void initializesEmptyQueueWithNullList() {
        // When
        queue.initializeQueue(null, 0);

        // Then
        assertThat(queue.getMemberIds()).isEmpty();
    }

    @Test
    void initializesEmptyQueueWithEmptyList() {
        // When
        queue.initializeQueue(List.of(), 0);

        // Then
        assertThat(queue.getMemberIds()).isEmpty();
    }

    @Test
    void getsNextAssigneeId() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);

        // When/Then
        assertThat(queue.getNextAssigneeId()).isEqualTo(1L);
    }

    @Test
    void getNextAssigneeIdReturnsNullForEmptyQueue() {
        // Given
        queue.initializeQueue(List.of(), 0);

        // When/Then
        assertThat(queue.getNextAssigneeId()).isNull();
    }

    @Test
    void rotatesQueue() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L, 3L);

        // When
        queue.rotate();

        // Then - first member moves to end
        assertThat(queue.getMemberIds()).containsExactly(2L, 3L, 1L);
    }

    @Test
    void rotateDoesNothingForSingleMember() {
        // Given
        queue.initializeQueue(Arrays.asList(user1), 0);

        // When
        queue.rotate();

        // Then
        assertThat(queue.getMemberIds()).containsExactly(1L);
    }

    @Test
    void addsMember() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2), 0);

        // When
        queue.addMember(user3);

        // Then
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void addMemberDoesNotAddDuplicate() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2), 0);

        // When
        queue.addMember(user1);

        // Then - still only 2 members
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L);
    }

    @Test
    void removesMember() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);

        // When
        queue.removeMember(user2);

        // Then
        assertThat(queue.getMemberIds()).containsExactly(1L, 3L);
    }

    @Test
    void swapsPositions() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);

        // When
        queue.swapPositions(1L, 3L);

        // Then
        assertThat(queue.getMemberIds()).containsExactly(3L, 2L, 1L);
    }

    @Test
    void swapPositionsDoesNothingForSameUser() {
        // Given
        queue.initializeQueue(Arrays.asList(user1, user2, user3), 0);

        // When
        queue.swapPositions(1L, 1L);

        // Then - unchanged
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void swapWithNextOccurrence() {
        // Given - queue with repeated pattern: [1, 2, 3, 1, 2, 3]
        queue.setMemberQueueOrder("1,2,3,1,2,3");

        // When - swap position 0 (which has user1) with next occurrence of user3
        boolean result = queue.swapWithNextOccurrence(0, 3L);

        // Then
        assertThat(result).isTrue();
        assertThat(queue.getMemberIds()).containsExactly(3L, 2L, 1L, 1L, 2L, 3L);
    }

    @Test
    void swapWithNextOccurrenceReturnsFalseIfTargetNotFound() {
        // Given
        queue.setMemberQueueOrder("1,2,3");

        // When - try to swap with user 4 who doesn't exist
        boolean result = queue.swapWithNextOccurrence(0, 4L);

        // Then
        assertThat(result).isFalse();
        assertThat(queue.getMemberIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void findsPositionFrom() {
        // Given
        queue.setMemberQueueOrder("1,2,3,1,2,3");

        // When/Then
        assertThat(queue.findPositionFrom(1L, 0)).isEqualTo(0);
        assertThat(queue.findPositionFrom(1L, 1)).isEqualTo(3);
        assertThat(queue.findPositionFrom(2L, 0)).isEqualTo(1);
        assertThat(queue.findPositionFrom(4L, 0)).isEqualTo(-1);
    }
}
