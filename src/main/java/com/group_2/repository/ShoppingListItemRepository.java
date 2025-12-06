package com.group_2.repository;

import com.model.ShoppingList;
import com.model.ShoppingListItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    /**
     * Find all items in a shopping list.
     */
    List<ShoppingListItem> findByShoppingList(ShoppingList shoppingList);
}
