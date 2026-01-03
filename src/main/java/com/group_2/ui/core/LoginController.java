package com.group_2.ui.core;

import com.group_2.dto.core.UserSummaryDTO;
import com.group_2.service.core.UserService;
import com.group_2.util.SessionManager;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Controller for handling user login functionality. Extends the abstract
 * Controller class to inherit common UI utilities.
 */
@Component
public class LoginController extends Controller {

    private final UserService userService;
    private final SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordButton;

    private boolean passwordVisible = false;

    public LoginController(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordButton.setText("◯");
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("◉");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();

        Optional<UserSummaryDTO> user = userService.authenticateSummary(email, password);
        if (user.isPresent()) {
            sessionManager.setCurrentUserSummary(user.get()); // Set session snapshot only
            navigateAfterAuth(user.get());
        } else {
            showErrorAlert("Login Failed", "Invalid email or password.");
        }
    }

    @FXML
    public void showSignupScreen() {
        loadScene(emailField.getScene(), "/core/signup.fxml");
    }

    private void navigateAfterAuth(UserSummaryDTO user) {
        // Check if user has a WG
        if (user.wgId() != null) {
            // User has a WG - go to main screen
            loadScene(emailField.getScene(), "/core/main_screen.fxml");
            javafx.application.Platform.runLater(() -> {
                MainScreenController mainScreenController = applicationContext.getBean(MainScreenController.class);
                mainScreenController.initView();
            });
        } else {
            // User has no WG - go to no_wg screen
            loadScene(emailField.getScene(), "/core/no_wg.fxml");
            javafx.application.Platform.runLater(() -> {
                NoWgController noWgController = applicationContext.getBean(NoWgController.class);
                noWgController.initView();
            });
        }
    }
}
