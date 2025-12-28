package com.group_2.util;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.service.core.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Spring-managed session manager that maintains the current user state.
 * This is a singleton bean that holds the logged-in user for the application
 * session.
 */
@Component
public class SessionManager {

    private User currentUser;

    @Autowired
    private UserService userService;

    /**
     * Sets the currently logged-in user.
     *
     * @param user The User object representing the logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Retrieves the currently logged-in user.
     *
     * @return The User object representing the logged-in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Retrieves the user ID of the currently logged-in user.
     *
     * @return The user ID, or null if no user is logged in.
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Retrieves the WG of the currently logged-in user.
     *
     * @return The user's WG, or null if not set.
     */
    public WG getCurrentUserWG() {
        return currentUser != null ? currentUser.getWg() : null;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clears the session by resetting the current user to null.
     */
    public void clear() {
        currentUser = null;
    }

    /**
     * Refreshes the current user by fetching the latest data from the database.
     */
    public void refreshCurrentUser() {
        if (currentUser != null) {
            currentUser = userService.getUser(currentUser.getId()).orElse(null);
        }
    }
}