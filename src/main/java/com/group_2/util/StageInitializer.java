package com.group_2.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationEvent;

@Component
public class StageInitializer implements ApplicationListener<StageInitializer.StageReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StageInitializer.class);

    private final SpringFXMLLoader fxmlLoader;

    public StageInitializer(SpringFXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            Parent root = fxmlLoader.load("/core/login.fxml");

            // Set application icon
            stage.getIcons()
                    .add(new javafx.scene.image.Image(getClass().getResourceAsStream("/pictures/SE_Hommunity.png")));

            // Set appropriate initial window size
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("Melcher SE Projekt - Login");

            // Set minimum window size for usability
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.show();
        } catch (Exception e) {
            log.error("Failed to initialize application stage", e);
        }
    }

    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}
