package com.group_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group_2.model.WG;

@Repository
public interface WGRepository extends JpaRepository<WG, Long> {
    java.util.Optional<WG> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    // Excludes specific WG - used when regenerating an invite code
    boolean existsByInviteCodeAndIdNot(String inviteCode, Long excludeWgId);
}
