package com.group_2.ui.core;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;

import com.group_2.util.SpringFXMLLoader;

import java.io.IOException;
import java.util.Optional;

/**
 * Abstract base controller class for handling JavaFX scene management and
 * dialogs. Provides centralized alert/dialog creation with consistent styling.
 */
public abstract class Controller {

    @Autowired
    protected SpringFXMLLoader fxmlLoader;

    // ========== Window Utilities ==========

    // Gets owner window from any scene element for parenting dialogs
    protected Window getOwnerWindow(Node node) {
        if (node != null && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return null;
    }

    protected void configureDialogOwner(Dialog<?> dialog, Window owner) {
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    // Applies consistent styling to any Dialog
    protected void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("styled-dialog");
        try {
            String stylesheet = getClass().getResource("/css/styles.css").toExternalForm();
            if (!dialogPane.getStylesheets().contains(stylesheet)) {
                dialogPane.getStylesheets().add(stylesheet);
            }
        } catch (Exception e) {
            // Stylesheet not found, continue without custom styling
        }
    }

    // ========== Scene Navigation ==========

    protected void loadScene(javafx.scene.Scene currentScene, String fxmlPath) {
        try {
            Parent root = fxmlLoader.load(fxmlPath);
            currentScene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error loading page",
                    "Could not load the next page. Try again or close the application.\n" + e.getMessage());
        }
    }

    // ========== Typed Alert Methods ==========

    protected void showSuccessAlert(String title, String message) {
        showSuccessAlert(title, message, null);
    }

    protected void showSuccessAlert(String title, String message, Window owner) {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showErrorAlert(String title, String message) {
        showErrorAlert(title, message, null);
    }

    protected void showErrorAlert(String title, String message, Window owner) {
        Alert alert = createStyledAlert(Alert.AlertType.ERROR, owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showWarningAlert(String title, String message) {
        showWarningAlert(title, message, null);
    }

    protected void showWarningAlert(String title, String message, Window owner) {
        Alert alert = createStyledAlert(Alert.AlertType.WARNING, owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected boolean showConfirmDialog(String title, String header, String message) {
        return showConfirmDialog(title, header, message, null);
    }

    protected boolean showConfirmDialog(String title, String header, String message, Window owner) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    protected Optional<ButtonType> showConfirmDialogWithButtons(String title, String header, String message,
            Window owner, ButtonType... buttons) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        }
        return alert.showAndWait();
    }

    protected Alert createStyledConfirmDialog(String title, String header, String message, Window owner,
            ButtonType... buttons) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        }
        return alert;
    }

    // ========== Private Helpers ==========

    private Alert createStyledAlert(Alert.AlertType type, Window owner) {
        Alert alert = new Alert(type);
        configureDialogOwner(alert, owner);

        // Apply CSS styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("styled-alert");

        // Add type-specific style class
        switch (type) {
        case ERROR -> dialogPane.getStyleClass().add("alert-error");
        case INFORMATION -> dialogPane.getStyleClass().add("alert-success");
        case WARNING -> dialogPane.getStyleClass().add("alert-warning");
        case CONFIRMATION -> dialogPane.getStyleClass().add("alert-confirm");
        default -> {
        }
        }

        // Try to load application stylesheet
        try {
            String stylesheet = getClass().getResource("/css/styles.css").toExternalForm();
            dialogPane.getStylesheets().add(stylesheet);
        } catch (Exception e) {
            // Stylesheet not found, continue without custom styling
        }

        return alert;
    }
}
