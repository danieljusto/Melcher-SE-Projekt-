package com.group_2.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;

import com.group_2.util.SpringFXMLLoader;

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

    /**
     * Gets the owner window from any scene element.
     * This is used to properly parent dialogs for fullscreen compatibility.
     *
     * @param node Any node in the scene
     * @return The window containing the node, or null if not attached
     */
    protected Window getOwnerWindow(Node node) {
        if (node != null && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return null;
    }

    /**
     * Configures a dialog with the owner window for proper fullscreen behavior.
     *
     * @param dialog The dialog to configure
     * @param owner  The owner window
     */
    protected void configureDialogOwner(Dialog<?> dialog, Window owner) {
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

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
        showError(title, header, content, null);
    }

    /**
     * Displays an error dialog with the specified information and owner window.
     *
     * @param title   The title of the error dialog window
     * @param header  The header text shown in the error dialog
     * @param content The detailed error message to be displayed
     * @param owner   The owner window for proper fullscreen behavior
     */
    protected void showError(String title, String header, String content, Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        configureDialogOwner(alert, owner);
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
        showAlert(type, title, content, null);
    }

    /**
     * Displays an alert dialog with the specified type and owner window.
     *
     * @param type    The type of alert (ERROR, INFORMATION, WARNING, etc.)
     * @param title   The title of the dialog window
     * @param content The message to be displayed
     * @param owner   The owner window for proper fullscreen behavior
     */
    protected void showAlert(Alert.AlertType type, String title, String content, Window owner) {
        Alert alert = new Alert(type);
        configureDialogOwner(alert, owner);
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
        showConfirmation(title, header, content, null);
    }

    /**
     * Displays a confirmation dialog with options and owner window.
     *
     * @param title   The title of the confirmation dialog window
     * @param header  The header text shown in the confirmation dialog
     * @param content The detailed message or question to be displayed
     * @param owner   The owner window for proper fullscreen behavior
     */
    protected void showConfirmation(String title, String header, String content, Window owner) {
        ButtonType correctButton = new ButtonType("Korrigieren");
        ButtonType cancelButton = new ButtonType("Prozess abbrechen");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(alert, owner);
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

}
