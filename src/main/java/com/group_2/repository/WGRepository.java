package com.group_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group_2.model.WG;

@Repository
public interface WGRepository extends JpaRepository<WG, Long> {
    java.util.Optional<WG> findByInviteCode(String inviteCode);

    /**
     * Check if an invite code already exists in the database.
     */
    boolean existsByInviteCode(String inviteCode);

    /**
     * Check if an invite code already exists in the database, excluding a specific
     * WG.
     * This is used when regenerating an invite code for an existing WG.
     */
    boolean existsByInviteCodeAndIdNot(String inviteCode, Long excludeWgId);
}
