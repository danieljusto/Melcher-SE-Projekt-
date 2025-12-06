package com.group_2.ui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;

import com.group_2.util.SessionManager;
import com.group_2.util.SpringFXMLLoader;
import com.model.User;
import com.model.WG;

import java.io.IOException;

/**
 * Abstract base controller class for handling JavaFX scene management and error
 * display.
 * This class provides common functionality for all controllers in the
 * application.
 */
public abstract class Controller {

    @Autowired
    protected SpringFXMLLoader fxmlLoader;

    @Autowired
    protected SessionManager sessionManager;

    /**
     * Loads and displays a new JavaFX scene using Spring's FXML loader.
     *
     * @param currentScene The current scene to get the root from
     * @param fxmlPath     The path of the FXML file to load (e.g., "/login.fxml")
     */
    protected void loadScene(javafx.scene.Scene currentScene, String fxmlPath) {
        try {
            Parent root = fxmlLoader.load(fxmlPath);
            currentScene.setRoot(root);
        } catch (IOException e) {
            showError("Error", "Error loading page",
                    "Could not load the next page. Try again or close the application. \n" + e.getMessage());
        }
    }

    /**
     * Displays an error dialog with the specified information.
     * Creates and shows a JavaFX Alert dialog of type ERROR.
     *
     * @param title   The title of the error dialog window
     * @param header  The header text shown in the error dialog
     * @param content The detailed error message to be displayed
     */
    protected void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays an alert dialog with the specified type.
     *
     * @param type    The type of alert (ERROR, INFORMATION, WARNING, etc.)
     * @param title   The title of the dialog window
     * @param content The message to be displayed
     */
    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog with options to correct information or cancel
     * the process.
     * If the user chooses to cancel, the application will exit.
     *
     * @param title   The title of the confirmation dialog window
     * @param header  The header text shown in the confirmation dialog
     * @param content The detailed message or question to be displayed
     */
    protected void showConfirmation(String title, String header, String content) {
        ButtonType correctButton = new ButtonType("Korrigieren");
        ButtonType cancelButton = new ButtonType("Prozess abbrechen");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(correctButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == cancelButton) {
                Platform.exit();
            }
        });
    }

    // ========== Session Management Helper Methods ==========

    /**
     * Gets the currently logged-in user from the session.
     *
     * @return The current User, or null if not logged in
     */
    protected User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return The user ID, or null if not logged in
     */
    protected Long getCurrentUserId() {
        return sessionManager.getCurrentUserId();
    }

    /**
     * Gets the WG of the currently logged-in user.
     *
     * @return The user's WG, or null if not in a WG
     */
    protected WG getCurrentUserWG() {
        return sessionManager.getCurrentUserWG();
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    protected boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Sets the current user in the session.
     *
     * @param user The user to set as current
     */
    protected void setCurrentUser(User user) {
        sessionManager.setCurrentUser(user);
    }

    /**
     * Clears the current session (logs out the user).
     */
    protected void clearSession() {
        sessionManager.clear();
    }

    /**
     * Refreshes the current user data from the database.
     * Useful after operations that might have changed user data.
     */
    protected void refreshCurrentUser() {
        sessionManager.refreshCurrentUser();
    }
}
