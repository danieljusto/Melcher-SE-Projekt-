package com.group_2.repository.shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group_2.model.User;
import com.group_2.model.shopping.ShoppingList;

import java.util.List;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findByCreator(User creator);

    List<ShoppingList> findBySharedWithContaining(User user);

    // Created by OR shared with
    @Query("SELECT DISTINCT sl FROM ShoppingList sl LEFT JOIN sl.sharedWith sw "
            + "WHERE sl.creator = :user OR sw = :user")
    List<ShoppingList> findAllAccessibleByUser(@Param("user") User user);

    @Query("SELECT sl FROM ShoppingList sl WHERE sl.creator.id = :userId")
    List<ShoppingList> findByCreatorId(@Param("userId") Long userId);

    @Query("SELECT sl FROM ShoppingList sl JOIN sl.sharedWith sw WHERE sw.id = :userId")
    List<ShoppingList> findBySharedWithUserId(@Param("userId") Long userId);
}
