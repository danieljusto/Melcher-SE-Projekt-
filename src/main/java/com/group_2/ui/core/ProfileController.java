package com.group_2.ui.core;

import com.group_2.dto.core.LeaveWGStatus;
import com.group_2.dto.core.UserProfileViewDTO;
import com.group_2.service.core.CoreViewService;
import com.group_2.service.core.UserService;
import com.group_2.service.core.WGService;
import com.group_2.util.SessionManager;
import com.group_2.util.StringUtils;

import javafx.fxml.FXML;
import javafx.geometry.Insets;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Controller for the profile view. Shows user information and provides
 * logout/leave WG functionality.
 */
@Component
public class ProfileController extends Controller {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final SessionManager sessionManager;
    private final WGService wgService;
    private final UserService userService;
    private final CoreViewService coreViewService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Text avatarInitial;
    @FXML
    private Text userNameText;
    @FXML
    private Text userEmailText;
    @FXML
    private Text nameDisplayText;
    @FXML
    private Text emailDisplayText;
    @FXML
    private Text wgStatusText;
    @FXML
    private Text roleText;

    @Autowired
    public ProfileController(SessionManager sessionManager, WGService wgService, UserService userService,
            CoreViewService coreViewService) {
        this.sessionManager = sessionManager;
        this.wgService = wgService;
        this.userService = userService;
        this.coreViewService = coreViewService;
    }

    public void initView() {
        sessionManager.refreshCurrentUser();
        updateProfileInfo();
    }

    private void updateProfileInfo() {
        Long currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null) {
            return;
        }

        UserProfileViewDTO profile = coreViewService.getUserProfile(currentUserId);
        if (profile == null || profile.user() == null) {
            return;
        }

        String fullName = profile.user().displayName();
        userNameText.setText(fullName);
        nameDisplayText.setText(fullName);

        String email = profile.user().email() != null ? profile.user().email() : "No email";
        userEmailText.setText(email);
        emailDisplayText.setText(email);

        String initial = StringUtils.getInitial(profile.user().name());
        avatarInitial.setText(initial);

        if (profile.wg() != null) {
            wgStatusText.setText("Member of " + profile.wg().name());
            roleText.setText(profile.admin() ? "Admin" : "Member");
        } else {
            wgStatusText.setText("No WG");
            roleText.setText("-");
        }
    }

    @FXML
    public void handleEditName() {
        Long currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null)
            return;
        UserProfileViewDTO profile = coreViewService.getUserProfile(currentUserId);
        if (profile == null || profile.user() == null) {
            return;
        }

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Name");
        dialog.setHeaderText("Update your name");
        dialog.getDialogPane().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("styled-dialog");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField firstNameField = new TextField(profile.user().name());
        firstNameField.setPromptText("First Name");
        firstNameField.getStyleClass().addAll("dialog-field", "dialog-field-small");

        TextField lastNameField = new TextField(profile.user().surname() != null ? profile.user().surname() : "");
        lastNameField.setPromptText("Last Name");
        lastNameField.getStyleClass().addAll("dialog-field", "dialog-field-small");

        content.getChildren().addAll(new Text("First Name:"), firstNameField, new Text("Last Name:"), lastNameField);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new String[] { firstNameField.getText().trim(), lastNameField.getText().trim() };
            }
            return null;
        });

        dialog.showAndWait().ifPresent(names -> {
            if (names[0].isEmpty()) {
                showWarningAlert("Invalid Name", "First name cannot be empty.");
                return;
            }
            try {
                userService.updateUser(currentUserId, names[0], names[1].isEmpty() ? null : names[1],
                        profile.user().email());
                sessionManager.refreshCurrentUser();
                updateProfileInfo();
                showSuccessAlert("Success", "Name updated successfully!");
            } catch (Exception e) {
                log.error("Failed to update name for user: {}", currentUserId, e);
                showErrorAlert("Error", "Failed to update name: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleEditEmail() {
        Long currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null)
            return;
        UserProfileViewDTO profile = coreViewService.getUserProfile(currentUserId);
        if (profile == null || profile.user() == null) {
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Email");
        dialog.setHeaderText("Update your email address");
        dialog.getDialogPane().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("styled-dialog");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField emailField = new TextField(profile.user().email() != null ? profile.user().email() : "");
        emailField.setPromptText("Email address");
        emailField.getStyleClass().addAll("dialog-field", "dialog-field-small");

        content.getChildren().addAll(new Text("Email:"), emailField);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return emailField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(email -> {
            if (email.isEmpty()) {
                showWarningAlert("Invalid Email", "Email cannot be empty.");
                return;
            }
            if (!email.contains("@")) {
                showWarningAlert("Invalid Email", "Please enter a valid email address.");
                return;
            }
            try {
                userService.updateUser(currentUserId, profile.user().name(), profile.user().surname(), email);
                sessionManager.refreshCurrentUser();
                updateProfileInfo();
                showSuccessAlert("Success", "Email updated successfully!");
            } catch (Exception e) {
                log.error("Failed to update email for user: {}", currentUserId, e);
                showErrorAlert("Error", "Failed to update email: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleLogout() {
        sessionManager.clear();
        loadScene(avatarInitial.getScene(), "/core/login.fxml");
    }

    @FXML
    public void handleLeaveWG() {
        Long currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null) {
            showWarningAlert("Not Logged In", "You must be logged in to leave a WG.");
            return;
        }

        // Service handles all business logic: WG membership, admin check, balance check
        LeaveWGStatus status = wgService.checkUserCanLeaveWG(currentUserId);

        if (!status.canLeave()) {
            showErrorAlert("Cannot Leave WG", status.message());
            return;
        }

        // If user has positive balance, show warning first
        if (status.message() != null) {
            showWarningAlert("Balance Notice", status.message());
        }

        UserProfileViewDTO profile = coreViewService.getUserProfile(currentUserId);
        String wgName = profile != null && profile.wg() != null ? profile.wg().name() : "the WG";

        boolean confirmed = showConfirmDialog("Leave WG", "Are you sure you want to leave " + wgName + "?",
                "This action cannot be undone.");

        if (confirmed) {
            try {
                if (profile == null || profile.wg() == null) {
                    showErrorAlert("Error", "Could not retrieve WG information.");
                    return;
                }
                wgService.removeMitbewohner(profile.wg().id(), currentUserId);
                sessionManager.refreshCurrentUser();
                showSuccessAlert("Success", "You have left the WG.");
                loadScene(avatarInitial.getScene(), "/core/no_wg.fxml");
            } catch (Exception e) {
                log.error("Failed to leave WG for user: {}", currentUserId, e);
                showErrorAlert("Error", "Failed to leave WG: " + e.getMessage());
            }
        }
    }

    @FXML
    public void backToHome() {
        loadScene(avatarInitial.getScene(), "/core/main_screen.fxml");
        javafx.application.Platform.runLater(() -> {
            MainScreenController mainController = applicationContext.getBean(MainScreenController.class);
            mainController.initView();
        });
    }
}
