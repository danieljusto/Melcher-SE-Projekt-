package com.group_2.testsupport;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.cleaning.CleaningTask;
import com.group_2.model.cleaning.Room;
import com.group_2.model.finance.Transaction;
import com.group_2.model.shopping.ShoppingList;
import com.group_2.model.shopping.ShoppingListItem;

import java.time.LocalDate;

/**
 * Utility class for creating test entities.
 * Provides factory methods for common entities used in tests.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // Prevent instantiation
    }

    /**
     * Create a WG with a given name.
     */
    public static WG wg(String name) {
        WG wg = new WG();
        wg.setName(name);
        wg.regenerateInviteCode(); // Required: inviteCode is not-null in database
        return wg;
    }

    /**
     * Create a User with given email and associated WG.
     */
    public static User user(String email, WG wg) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test");
        user.setSurname("User");
        user.setPassword("password123");
        user.setWg(wg);
        return user;
    }

    /**
     * Create a User with full details.
     */
    public static User user(String name, String surname, String email, String password, WG wg) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);
        user.setWg(wg);
        return user;
    }

    /**
     * Create a Room with a given name.
     */
    public static Room room(String name) {
        return new Room(name);
    }

    /**
     * Create a Room with a given name and WG.
     */
    public static Room room(String name, WG wg) {
        return new Room(name, wg);
    }

    /**
     * Create a Transaction with given parameters.
     */
    public static Transaction transaction(User creditor, Double amount, String description, WG wg) {
        return new Transaction(creditor, creditor, amount, description, wg);
    }

    /**
     * Create a ShoppingList with a given name and creator.
     */
    public static ShoppingList shoppingList(String name, User creator) {
        return new ShoppingList(name, creator);
    }

    /**
     * Create a ShoppingListItem.
     */
    public static ShoppingListItem shoppingListItem(String name, User creator, ShoppingList shoppingList) {
        return new ShoppingListItem(name, creator, shoppingList);
    }

    /**
     * Create a CleaningTask with given parameters.
     */
    public static CleaningTask cleaningTask(Room room, User assignee, WG wg, LocalDate weekStartDate) {
        return new CleaningTask(room, assignee, wg, weekStartDate);
    }
}
