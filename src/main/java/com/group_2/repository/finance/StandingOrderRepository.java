package com.group_2.repository.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group_2.model.WG;
import com.group_2.model.finance.StandingOrder;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StandingOrderRepository extends JpaRepository<StandingOrder, Long> {

    List<StandingOrder> findByWg(WG wg);

    List<StandingOrder> findByNextExecutionLessThanEqualAndIsActiveTrue(LocalDate date);

    // With lock to prevent double-execution when scheduler runs concurrently
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StandingOrder s WHERE s.nextExecution <= :date AND s.isActive = true")
    List<StandingOrder> findDueOrdersForUpdate(@Param("date") LocalDate date);

    List<StandingOrder> findByWgAndIsActiveTrue(WG wg);

    @Query("SELECT s FROM StandingOrder s WHERE s.isActive = true AND (s.creditor.id = :userId OR s.createdBy.id = :userId)")
    List<StandingOrder> findActiveByCreditorOrCreator(@Param("userId") Long userId);

    @Query("SELECT s FROM StandingOrder s WHERE s.isActive = true AND s.wg.id = :wgId")
    List<StandingOrder> findActiveByWgId(@Param("wgId") Long wgId);

    void deleteByWg(WG wg);
}
