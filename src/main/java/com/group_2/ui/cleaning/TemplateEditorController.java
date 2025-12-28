package com.group_2.ui.cleaning;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.cleaning.CleaningTaskTemplate;
import com.group_2.model.cleaning.RecurrenceInterval;
import com.group_2.model.cleaning.Room;
import com.group_2.service.cleaning.CleaningScheduleService;
import com.group_2.ui.core.Controller;
import com.group_2.ui.core.MainScreenController;
import com.group_2.ui.core.NavbarController;
import com.group_2.util.SessionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the template editor view.
 * Allows users to define a default weekly schedule template.
 * Uses a working copy that is only saved when "Save & Apply" is clicked.
 */
@Component
public class TemplateEditorController extends Controller {

    private final CleaningScheduleService cleaningScheduleService;
    private final SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Text headerTitle;
    @FXML
    private Text templateCountText;

    // Navbar
    @FXML
    private NavbarController navbarController;

    // Day columns
    @FXML
    private VBox mondayColumn;
    @FXML
    private VBox tuesdayColumn;
    @FXML
    private VBox wednesdayColumn;
    @FXML
    private VBox thursdayColumn;
    @FXML
    private VBox fridayColumn;
    @FXML
    private VBox saturdayColumn;
    @FXML
    private VBox sundayColumn;

    // Working copy of templates (not saved until Save & Apply)
    private List<WorkingTemplate> workingTemplates = new ArrayList<>();
    private boolean hasUnsavedChanges = false;

    /**
     * A working copy of a template that may or may not exist in the database yet.
     */
    private static class WorkingTemplate {
        Room room;
        int dayOfWeek;
        RecurrenceInterval recurrenceInterval;
        boolean isDeleted = false; // marks for deletion on save

        WorkingTemplate(CleaningTaskTemplate template) {
            this.room = template.getRoom();
            this.dayOfWeek = template.getDayOfWeek();
            this.recurrenceInterval = template.getRecurrenceInterval();
        }

        WorkingTemplate(Room room, DayOfWeek day, RecurrenceInterval interval) {
            this.room = room;
            this.dayOfWeek = day.getValue();
            this.recurrenceInterval = interval;
        }
    }

    public TemplateEditorController(CleaningScheduleService cleaningScheduleService, SessionManager sessionManager) {
        this.cleaningScheduleService = cleaningScheduleService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        if (navbarController != null) {
            navbarController.setTitle("📝 Template Editor");
            navbarController.setBackDestination("/cleaning/cleaning_schedule.fxml", false);
            navbarController.getBackButton().setOnAction(e -> backToCleaningSchedule());
        }
        loadWorkingCopy();
        refreshView();
    }

    /**
     * Load templates from database into working copy.
     */
    private void loadWorkingCopy() {
        workingTemplates.clear();
        hasUnsavedChanges = false;

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        WG wg = currentUser.getWg();
        List<CleaningTaskTemplate> templates = cleaningScheduleService.getTemplates(wg);

        for (CleaningTaskTemplate template : templates) {
            workingTemplates.add(new WorkingTemplate(template));
        }
    }

    /**
     * Refresh the UI from the working copy.
     */
    private void refreshView() {
        clearColumns();

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        WG wg = currentUser.getWg();

        // Count non-deleted templates
        long count = workingTemplates.stream().filter(t -> !t.isDeleted).count();
        templateCountText.setText(String.valueOf(count));

        for (WorkingTemplate template : workingTemplates) {
            if (template.isDeleted)
                continue;

            VBox column = getColumnForDay(template.dayOfWeek);
            if (column != null) {
                column.getChildren().add(createTemplateCard(template, wg));
            }
        }
    }

    private void clearColumns() {
        VBox[] columns = { mondayColumn, tuesdayColumn, wednesdayColumn,
                thursdayColumn, fridayColumn, saturdayColumn, sundayColumn };
        for (VBox column : columns) {
            if (column.getChildren().size() > 1) {
                column.getChildren().subList(1, column.getChildren().size()).clear();
            }
        }
    }

    private VBox getColumnForDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return mondayColumn;
            case 2:
                return tuesdayColumn;
            case 3:
                return wednesdayColumn;
            case 4:
                return thursdayColumn;
            case 5:
                return fridayColumn;
            case 6:
                return saturdayColumn;
            case 7:
                return sundayColumn;
            default:
                return mondayColumn;
        }
    }

    private VBox createTemplateCard(WorkingTemplate template, WG wg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10;");

        // Room name header
        Text roomName = new Text(template.room.getName());
        roomName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #1e293b;");
        roomName.setWrappingWidth(130);

        // Show round-robin info
        HBox assigneeRow = new HBox(5);
        assigneeRow.setAlignment(Pos.CENTER_LEFT);
        Text rotationInfo = new Text("🔄 Round-robin");
        rotationInfo.setStyle("-fx-font-size: 11px; -fx-fill: #64748b;");
        assigneeRow.getChildren().add(rotationInfo);

        // Frequency display
        HBox frequencyRow = new HBox(5);
        frequencyRow.setAlignment(Pos.CENTER_LEFT);
        Text freqIcon = new Text("📅");
        freqIcon.setStyle("-fx-font-size: 11px;");
        Text freqText = new Text(template.recurrenceInterval.getDisplayName());
        freqText.setStyle("-fx-font-size: 11px; -fx-fill: #64748b;");
        frequencyRow.getChildren().addAll(freqIcon, freqText);

        // Separator
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Action buttons
        VBox actions = new VBox(6);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️ Edit Day");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-font-size: 12px; " +
                "-fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;");
        editBtn.setTooltip(new Tooltip("Edit day of week"));
        editBtn.setOnAction(e -> showEditTemplateDialog(template));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 12px; " +
                "-fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;");
        deleteBtn.setTooltip(new Tooltip("Delete this task"));
        deleteBtn.setOnAction(e -> deleteTemplate(template));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(roomName, assigneeRow, frequencyRow, separator, actions);
        return card;
    }

    @FXML
    public void showAddTemplateDialog() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "You must be in a WG.", getOwnerWindow(headerTitle));
            return;
        }

        WG wg = currentUser.getWg();
        if (wg.rooms == null || wg.rooms.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Rooms", "Please add rooms first in the Dashboard.",
                    getOwnerWindow(headerTitle));
            return;
        }

        if (wg.getMitbewohner().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Members", "WG has no members.",
                    getOwnerWindow(headerTitle));
            return;
        }

        Dialog<WorkingTemplate> dialog = new Dialog<>();
        configureDialogOwner(dialog, getOwnerWindow(headerTitle));
        dialog.setTitle("Add Template Task");
        dialog.setHeaderText("Add a room to the cleaning schedule");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Room selection
        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.getItems().addAll(wg.rooms);
        roomCombo.setPromptText("Select a room");
        roomCombo.setConverter(new javafx.util.StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room != null ? room.getName() : "";
            }

            @Override
            public Room fromString(String s) {
                return null;
            }
        });

        // Day selection
        ComboBox<DayOfWeek> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll(DayOfWeek.values());
        dayCombo.setPromptText("Day of week");
        dayCombo.setConverter(new javafx.util.StringConverter<DayOfWeek>() {
            @Override
            public String toString(DayOfWeek d) {
                if (d == null)
                    return "";
                return d.toString().substring(0, 1) + d.toString().substring(1).toLowerCase();
            }

            @Override
            public DayOfWeek fromString(String s) {
                return null;
            }
        });

        // Frequency selection
        ComboBox<RecurrenceInterval> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(RecurrenceInterval.values());
        freqCombo.setValue(RecurrenceInterval.WEEKLY);
        freqCombo.setPromptText("Frequency");

        // Info about round-robin
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-padding: 10;");
        Text infoIcon = new Text("🔄");
        infoIcon.setStyle("-fx-font-size: 14px;");
        Text infoText = new Text("Assignees rotate automatically each week");
        infoText.setStyle("-fx-font-size: 12px; -fx-fill: #166534;");
        infoBox.getChildren().addAll(infoIcon, infoText);

        content.getChildren().addAll(
                new Text("Room:"), roomCombo,
                new Text("Day:"), dayCombo,
                new Text("Frequency:"), freqCombo,
                infoBox);

        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().lookupButton(addButtonType).setDisable(true);
        roomCombo.valueProperty().addListener(
                (o, oldV, newV) -> updateAddButton(dialog, addButtonType, roomCombo, dayCombo));
        dayCombo.valueProperty().addListener(
                (o, oldV, newV) -> updateAddButton(dialog, addButtonType, roomCombo, dayCombo));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new WorkingTemplate(roomCombo.getValue(), dayCombo.getValue(), freqCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            workingTemplates.add(t);
            hasUnsavedChanges = true;
            refreshView();
        });
    }

    private void updateAddButton(Dialog<?> dialog, ButtonType btnType, ComboBox<Room> room,
            ComboBox<DayOfWeek> day) {
        dialog.getDialogPane().lookupButton(btnType).setDisable(
                room.getValue() == null || day.getValue() == null);
    }

    private void showEditTemplateDialog(WorkingTemplate template) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        configureDialogOwner(dialog, getOwnerWindow(headerTitle));
        dialog.setTitle("Edit Template");
        dialog.setHeaderText("Edit \"" + template.room.getName() + "\"");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Day selection
        ComboBox<DayOfWeek> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll(DayOfWeek.values());
        dayCombo.setValue(DayOfWeek.of(template.dayOfWeek));
        dayCombo.setConverter(new javafx.util.StringConverter<DayOfWeek>() {
            @Override
            public String toString(DayOfWeek d) {
                if (d == null)
                    return "";
                return d.toString().substring(0, 1) + d.toString().substring(1).toLowerCase();
            }

            @Override
            public DayOfWeek fromString(String s) {
                return null;
            }
        });

        // Frequency selection
        ComboBox<RecurrenceInterval> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(RecurrenceInterval.values());
        freqCombo.setValue(template.recurrenceInterval);

        // Info about round-robin
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-padding: 10;");
        Text infoIcon = new Text("🔄");
        infoIcon.setStyle("-fx-font-size: 14px;");
        Text infoText = new Text("Assignees rotate automatically - no need to change");
        infoText.setStyle("-fx-font-size: 12px; -fx-fill: #166534;");
        infoBox.getChildren().addAll(infoIcon, infoText);

        content.getChildren().addAll(
                new Text("Day of Week:"), dayCombo,
                new Text("Frequency:"), freqCombo,
                infoBox);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                template.dayOfWeek = dayCombo.getValue().getValue();
                template.recurrenceInterval = freqCombo.getValue();
                hasUnsavedChanges = true;
            }
            return null;
        });

        dialog.showAndWait();
        refreshView();
    }

    private void deleteTemplate(WorkingTemplate template) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirm, getOwnerWindow(headerTitle));
        confirm.setTitle("Delete Template");
        confirm.setHeaderText("Delete \"" + template.room.getName() + "\"?");
        confirm.setContentText("This change will be applied when you click 'Save & Apply'.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                template.isDeleted = true;
                hasUnsavedChanges = true;
                refreshView();
            }
        });
    }

    @FXML
    public void saveAndApplyTemplate() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "You must be in a WG.", getOwnerWindow(headerTitle));
            return;
        }

        WG wg = currentUser.getWg();

        // First, clear all existing templates and tasks
        cleaningScheduleService.clearTemplates(wg);

        // Then, add all non-deleted templates from working copy
        for (WorkingTemplate wt : workingTemplates) {
            if (wt.isDeleted)
                continue;
            cleaningScheduleService.addTemplate(wg, wt.room, DayOfWeek.of(wt.dayOfWeek), wt.recurrenceInterval);
        }

        hasUnsavedChanges = false;

        showAlert(Alert.AlertType.INFORMATION, "Template Applied",
                "The template has been saved and applied to the schedule.", getOwnerWindow(headerTitle));

        // Navigate back to cleaning schedule to show the result
        loadScene(headerTitle.getScene(), "/cleaning/cleaning_schedule.fxml");
    }

    @FXML
    public void clearAllTemplates() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirm, getOwnerWindow(headerTitle));
        confirm.setTitle("Clear All");
        confirm.setHeaderText("Clear all template tasks?");
        confirm.setContentText("This change will be applied when you click 'Save & Apply'.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Mark all as deleted
                for (WorkingTemplate wt : workingTemplates) {
                    wt.isDeleted = true;
                }
                hasUnsavedChanges = true;
                refreshView();
            }
        });
    }

    @FXML
    public void backToHome() {
        if (hasUnsavedChanges) {
            if (!confirmDiscardChanges()) {
                return;
            }
        }
        loadScene(headerTitle.getScene(), "/core/main_screen.fxml");
        javafx.application.Platform.runLater(() -> {
            MainScreenController controller = applicationContext.getBean(MainScreenController.class);
            controller.initView();
        });
    }

    @FXML
    public void backToCleaningSchedule() {
        if (hasUnsavedChanges) {
            if (!confirmDiscardChanges()) {
                return;
            }
        }
        loadScene(headerTitle.getScene(), "/cleaning/cleaning_schedule.fxml");
    }

    private boolean confirmDiscardChanges() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirm, getOwnerWindow(headerTitle));
        confirm.setTitle("Unsaved Changes");
        confirm.setHeaderText("You have unsaved changes");
        confirm.setContentText("Do you want to discard your changes and go back?");

        ButtonType discardButton = new ButtonType("Discard Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(discardButton, cancelButton);

        return confirm.showAndWait().orElse(cancelButton) == discardButton;
    }

    @FXML
    public void goToCleaningSchedule() {
        backToCleaningSchedule();
    }
}
