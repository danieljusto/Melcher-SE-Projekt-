package com.group_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group_2.model.User;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    java.util.List<User> findByWgId(Long wgId);

    long countByWgId(Long wgId);

    boolean existsByIdAndWgId(Long id, Long wgId);

    // Simple email lookup
    Optional<User> findByEmail(String email);

    // Pessimistic lock for registration to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);
}
