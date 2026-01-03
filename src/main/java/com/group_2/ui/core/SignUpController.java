package com.group_2.ui.core;

import com.group_2.dto.core.UserSummaryDTO;
import com.group_2.service.core.UserService;
import com.group_2.util.SessionManager;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Controller for handling user signup functionality. Extends the abstract
 * Controller class to inherit common UI utilities.
 */
@Component
public class SignUpController extends Controller {

    private static final Logger log = LoggerFactory.getLogger(SignUpController.class);

    private final UserService userService;
    private final SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TextField signupNameField;
    @FXML
    private TextField signupSurnameField;
    @FXML
    private TextField signupEmailField;
    @FXML
    private PasswordField signupPasswordField;
    @FXML
    private TextField signupPasswordTextField;
    @FXML
    private Button togglePasswordButton;

    private boolean passwordVisible = false;

    public SignUpController(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            signupPasswordTextField.setText(signupPasswordField.getText());
            signupPasswordField.setVisible(false);
            signupPasswordField.setManaged(false);
            signupPasswordTextField.setVisible(true);
            signupPasswordTextField.setManaged(true);
            togglePasswordButton.setText("◯");
        } else {
            signupPasswordField.setText(signupPasswordTextField.getText());
            signupPasswordTextField.setVisible(false);
            signupPasswordTextField.setManaged(false);
            signupPasswordField.setVisible(true);
            signupPasswordField.setManaged(true);
            togglePasswordButton.setText("◉");
        }
    }

    @FXML
    public void handleSignup() {
        String name = signupNameField.getText().trim();
        String surname = signupSurnameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = passwordVisible ? signupPasswordTextField.getText() : signupPasswordField.getText();

        // Validate that all fields are filled
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        try {
            UserSummaryDTO user = userService.registerUserSummary(name, surname, email, password);
            sessionManager.setCurrentUserSummary(user); // Set session snapshot only
            showSuccessAlert("Signup Successful", "Account created!");
            // New users never have a WG, so go to no_wg screen
            loadScene(signupNameField.getScene(), "/core/no_wg.fxml");
            javafx.application.Platform.runLater(() -> {
                NoWgController noWgController = applicationContext.getBean(NoWgController.class);
                noWgController.initView();
            });
        } catch (Exception e) {
            log.error("User registration failed for email: {}", email, e);
            showErrorAlert("Signup Failed", e.getMessage());
        }
    }

    @FXML
    public void showLoginScreen() {
        loadScene(signupNameField.getScene(), "/core/login.fxml");
    }
}
