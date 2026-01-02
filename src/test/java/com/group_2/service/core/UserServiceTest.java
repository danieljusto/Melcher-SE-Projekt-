package com.group_2.service.core;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.repository.UserRepository;
import com.group_2.repository.WGRepository;
import com.group_2.testsupport.TestDataFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WGRepository wgRepository;

    @Test
    void registersUser() {
        // When
        User saved = userService.registerUser("John", "Doe", "john@example.com", "password123");

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getSurname()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        // Password should be hashed
        assertThat(saved.getPassword()).isNotEqualTo("password123");
    }

    @Test
    void registerUserThrowsOnDuplicateEmail() {
        // Given
        userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When/Then
        assertThatThrownBy(() -> userService.registerUser("Jane", "Doe", "john@example.com", "password456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void authenticatesUser() {
        // Given
        userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When
        Optional<User> authenticated = userService.authenticate("john@example.com", "password123");

        // Then
        assertThat(authenticated).isPresent();
        assertThat(authenticated.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void authenticationFailsWithWrongPassword() {
        // Given
        userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When
        Optional<User> authenticated = userService.authenticate("john@example.com", "wrongpassword");

        // Then
        assertThat(authenticated).isEmpty();
    }

    @Test
    void authenticationFailsWithNonexistentEmail() {
        // When
        Optional<User> authenticated = userService.authenticate("nonexistent@example.com", "password123");

        // Then
        assertThat(authenticated).isEmpty();
    }

    @Test
    void getsUserById() {
        // Given
        User saved = userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When
        Optional<User> found = userService.getUser(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void updatesUser() {
        // Given
        User saved = userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When
        User updated = userService.updateUser(saved.getId(), "Jane", "Smith", "jane@example.com");

        // Then
        assertThat(updated.getName()).isEqualTo("Jane");
        assertThat(updated.getSurname()).isEqualTo("Smith");
        assertThat(updated.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void deletesUser() {
        // Given
        User saved = userService.registerUser("John", "Doe", "john@example.com", "password123");
        Long userId = saved.getId();

        // When
        userService.deleteUser(userId);

        // Then
        assertThat(userService.getUser(userId)).isEmpty();
    }

    @Test
    void getsDisplayName() {
        // Given
        User saved = userService.registerUser("John", "Doe", "john@example.com", "password123");

        // When
        String displayName = userService.getDisplayName(saved.getId());

        // Then
        assertThat(displayName).isEqualTo("John D.");
    }

    @Test
    void getDisplayNameReturnsUnknownForMissingUser() {
        // When
        String displayName = userService.getDisplayName(999L);

        // Then
        assertThat(displayName).isEqualTo("Unknown");
    }
}
