package com.group_2.ui.core;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller for the reusable navigation bar component. This navbar can be
 * included in any view that needs a back button and title.
 */
@Component
@Scope("prototype")
public class NavbarController extends Controller {

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Text titleText;

    @FXML
    private Button backButton;

    private String backDestination = "/core/main_screen.fxml";
    private boolean initMainScreen = true;

    public void setTitle(String title) {
        titleText.setText(title);
    }

    public void setBackDestination(String destination, boolean initMainScreen) {
        this.backDestination = destination;
        this.initMainScreen = initMainScreen;
    }

    @FXML
    public void backToHome() {
        loadScene(titleText.getScene(), backDestination);
        if (initMainScreen && "/core/main_screen.fxml".equals(backDestination)) {
            javafx.application.Platform.runLater(() -> {
                MainScreenController controller = applicationContext.getBean(MainScreenController.class);
                controller.initView();
            });
        }
    }

    public Button getBackButton() {
        return backButton;
    }
}
