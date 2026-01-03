package com.group_2.ui.cleaning;

import com.group_2.dto.cleaning.CleaningTaskTemplateDTO;
import com.group_2.dto.cleaning.RoomDTO;
import com.group_2.dto.cleaning.WorkingTemplateDTO;
import com.group_2.dto.core.UserSessionDTO;
import com.group_2.dto.core.UserSummaryDTO;
import com.group_2.model.cleaning.RecurrenceInterval;
import com.group_2.service.cleaning.CleaningScheduleService;
import com.group_2.service.core.HouseholdSetupService;
import com.group_2.ui.core.Controller;
import com.group_2.ui.core.MainScreenController;
import com.group_2.ui.core.NavbarController;
import com.group_2.util.MonthlyScheduleUtil;
import com.group_2.util.SessionManager;

import javafx.event.ActionEvent;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the template editor view. Allows users to define a default
 * weekly schedule template. Uses a working copy that is only saved when "Save &
 * Apply" is clicked.
 */
@Component
public class TemplateEditorController extends Controller {

    private final CleaningScheduleService cleaningScheduleService;
    private final HouseholdSetupService householdSetupService;
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
    private List<WorkingTemplateDTO> workingTemplates = new ArrayList<>();
    private boolean hasUnsavedChanges = false;

    public TemplateEditorController(CleaningScheduleService cleaningScheduleService,
            HouseholdSetupService householdSetupService, SessionManager sessionManager) {
        this.cleaningScheduleService = cleaningScheduleService;
        this.householdSetupService = householdSetupService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        if (navbarController != null) {
            navbarController.setTitle("Template Editor");
            navbarController.setBackDestination("/cleaning/cleaning_schedule.fxml", false);
            navbarController.getBackButton().setOnAction(e -> backToCleaningSchedule());
        }
        loadWorkingCopy();
        configureDayColumnsResponsive();
        refreshView();
    }

    private void configureDayColumnsResponsive() {
        VBox[] columns = { mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn, saturdayColumn, sundayColumn };

        for (VBox col : columns) {
            col.setMinWidth(160);
            col.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(col, Priority.ALWAYS);
            col.setFillWidth(true);
        }
    }

    /**
     * Load templates from database into working copy.
     */
    private void loadWorkingCopy() {
        workingTemplates.clear();
        hasUnsavedChanges = false;

        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null)
            return;

        List<CleaningTaskTemplateDTO> templates = cleaningScheduleService.getTemplatesDTO(session.wgId());

        for (CleaningTaskTemplateDTO dto : templates) {
            workingTemplates.add(new WorkingTemplateDTO(dto));
        }
    }

    /**
     * Refresh the UI from the working copy.
     */
    private void refreshView() {
        clearColumns();

        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null)
            return;

        // Count non-deleted templates
        long count = workingTemplates.stream().filter(t -> !t.isDeleted()).count();
        templateCountText.setText(String.valueOf(count));

        for (WorkingTemplateDTO template : workingTemplates) {
            if (template.isDeleted())
                continue;

            VBox column = getColumnForDay(template.getDayOfWeek());
            if (column != null) {
                column.getChildren().add(createTemplateCard(template));
            }
        }
    }

    private void clearColumns() {
        VBox[] columns = { mondayColumn, tuesdayColumn, wednesdayColumn, thursdayColumn, fridayColumn, saturdayColumn,
                sundayColumn };
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

    private VBox createTemplateCard(WorkingTemplateDTO template) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("template-card");
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);

        // Room name header
        Text roomName = new Text(template.getRoomName());
        roomName.getStyleClass().add("template-room-name");
        roomName.wrappingWidthProperty().bind(card.widthProperty().subtract(24));

        // Show round-robin info
        HBox assigneeRow = new HBox(5);
        assigneeRow.setAlignment(Pos.CENTER_LEFT);
        Text rotationInfo = new Text("Round-robin");
        rotationInfo.getStyleClass().add("template-info-text");
        assigneeRow.getChildren().add(rotationInfo);

        // Frequency display
        HBox frequencyRow = new HBox(5);
        frequencyRow.setAlignment(Pos.CENTER_LEFT);
        Text freqIcon = new Text("");
        freqIcon.getStyleClass().add("template-icon");
        Text freqText = new Text(template.getRecurrenceInterval().getDisplayName());
        freqText.getStyleClass().add("template-info-text");
        frequencyRow.getChildren().addAll(freqIcon, freqText);

        // Separator
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.getStyleClass().add("divider-line");

        // Action buttons
        VBox actions = new VBox(6);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Edit");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.getStyleClass().addAll("small-action-button", "edit-button");
        editBtn.setTooltip(new Tooltip("Edit day of week"));
        editBtn.setOnAction(e -> showEditTemplateDialog(template));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.getStyleClass().addAll("small-action-button", "delete-button-small");
        deleteBtn.setTooltip(new Tooltip("Delete this task"));
        deleteBtn.setOnAction(e -> deleteTemplate(template));

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(roomName, assigneeRow, frequencyRow, separator, actions);
        return card;
    }

    @FXML
    public void showAddTemplateDialog() {
        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null) {
            showErrorAlert("Error", "You must be in a WG.", getOwnerWindow(headerTitle));
            return;
        }

        Long wgId = session.wgId();
        java.util.List<RoomDTO> rooms = householdSetupService.getRoomsForWgDTO(wgId);
        if (rooms.isEmpty()) {
            showWarningAlert("No Rooms", "Please add rooms first in the Dashboard.", getOwnerWindow(headerTitle));
            return;
        }

        List<UserSummaryDTO> members = cleaningScheduleService.getMemberSummaries(wgId);
        if (members.isEmpty()) {
            showWarningAlert("No Members", "WG has no members.", getOwnerWindow(headerTitle));
            return;
        }

        Dialog<WorkingTemplateDTO> dialog = new Dialog<>();
        configureDialogOwner(dialog, getOwnerWindow(headerTitle));
        styleDialog(dialog);
        dialog.setTitle("Add Template Task");
        dialog.setHeaderText("Add a room to the cleaning schedule");
        dialog.getDialogPane().setPrefWidth(520);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Room selection
        ComboBox<RoomDTO> roomCombo = new ComboBox<>();
        roomCombo.getItems().addAll(rooms);
        roomCombo.setPromptText("Select a room");
        roomCombo.setConverter(new javafx.util.StringConverter<RoomDTO>() {
            @Override
            public String toString(RoomDTO room) {
                return room != null ? room.name() : "";
            }

            @Override
            public RoomDTO fromString(String s) {
                return null;
            }
        });

        // Frequency selection
        ComboBox<RecurrenceInterval> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(RecurrenceInterval.values());
        freqCombo.setValue(RecurrenceInterval.WEEKLY);
        freqCombo.setPromptText("Frequency");
        freqCombo.getStyleClass().add("dialog-field");
        freqCombo.setPrefWidth(150);

        Text freqLabel = new Text("Frequency");
        freqLabel.getStyleClass().add("dialog-label-secondary");
        VBox freqBox = new VBox(4, freqLabel, freqCombo);

        // Day picker (date selection)
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select...");
        datePicker.getStyleClass().add("dialog-field");
        datePicker.setPrefWidth(160);

        Text dateLabel = new Text("Day");
        dateLabel.getStyleClass().add("dialog-label-secondary");
        VBox dateBox = new VBox(4, dateLabel, datePicker);

        // Monthly "last day" option
        Text lastDaySpacer = new Text(" ");
        lastDaySpacer.getStyleClass().add("dialog-label-muted");
        CheckBox lastDayCheckbox = new CheckBox("Last day of month");
        lastDayCheckbox.getStyleClass().add("dialog-label-muted");
        VBox lastDayBox = new VBox(4, lastDaySpacer, lastDayCheckbox);
        lastDayBox.setAlignment(Pos.CENTER_LEFT);
        lastDayBox.setVisible(false);
        lastDayBox.setManaged(false);

        HBox scheduleRow = new HBox(12, freqBox, dateBox, lastDayBox);
        scheduleRow.setAlignment(Pos.CENTER_LEFT);

        updateScheduleControls(freqCombo.getValue(), dateLabel, dateBox, lastDayCheckbox, lastDayBox);

        // Info about round-robin
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getStyleClass().add("info-box-success");
        Text infoIcon = new Text("i");
        infoIcon.getStyleClass().add("info-box-success-icon");
        Text infoText = new Text("Assignees rotate automatically each week");
        infoText.getStyleClass().add("info-box-success-text");
        infoBox.getChildren().addAll(infoIcon, infoText);

        content.getChildren().addAll(new Text("Room:"), roomCombo, scheduleRow, infoBox);

        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().lookupButton(addButtonType).setDisable(true);
        roomCombo.valueProperty().addListener(
                (o, oldV, newV) -> updateAddButton(dialog, addButtonType, roomCombo, freqCombo, datePicker,
                        lastDayCheckbox));
        datePicker.valueProperty().addListener(
                (o, oldV, newV) -> updateAddButton(dialog, addButtonType, roomCombo, freqCombo, datePicker,
                        lastDayCheckbox));
        freqCombo.valueProperty().addListener((o, oldV, newV) -> {
            updateScheduleControls(newV, dateLabel, dateBox, lastDayCheckbox, lastDayBox);
            updateAddButton(dialog, addButtonType, roomCombo, freqCombo, datePicker, lastDayCheckbox);
        });
        lastDayCheckbox.selectedProperty().addListener((o, oldV, newV) -> {
            updateScheduleControls(freqCombo.getValue(), dateLabel, dateBox, lastDayCheckbox, lastDayBox);
            updateAddButton(dialog, addButtonType, roomCombo, freqCombo, datePicker, lastDayCheckbox);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                RoomDTO selectedRoom = roomCombo.getValue();
                LocalDate baseDate = MonthlyScheduleUtil.resolveBaseDate(datePicker.getValue(), freqCombo.getValue(),
                        lastDayCheckbox.isSelected(), null);
                if (baseDate == null) {
                    return null;
                }
                return new WorkingTemplateDTO(selectedRoom.id(), selectedRoom.name(), baseDate, freqCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            workingTemplates.add(t);
            hasUnsavedChanges = true;
            refreshView();
        });
    }

    private void updateAddButton(Dialog<?> dialog, ButtonType btnType, ComboBox<RoomDTO> room,
            ComboBox<RecurrenceInterval> freq, DatePicker datePicker, CheckBox lastDayCheckbox) {
        boolean needsDate = MonthlyScheduleUtil.isDateRequired(freq.getValue(), lastDayCheckbox.isSelected());
        boolean hasDate = datePicker.getValue() != null;
        dialog.getDialogPane().lookupButton(btnType)
                .setDisable(room.getValue() == null || (needsDate && !hasDate));
    }

    private void updateSaveButton(Dialog<?> dialog, ButtonType btnType, ComboBox<RecurrenceInterval> freq,
            DatePicker datePicker, CheckBox lastDayCheckbox) {
        boolean needsDate = MonthlyScheduleUtil.isDateRequired(freq.getValue(), lastDayCheckbox.isSelected());
        boolean hasDate = datePicker.getValue() != null;
        dialog.getDialogPane().lookupButton(btnType).setDisable(needsDate && !hasDate);
    }

    private void updateScheduleControls(RecurrenceInterval interval, Text dateLabel, VBox dateBox,
            CheckBox lastDayCheckbox, VBox lastDayBox) {
        boolean isMonthly = interval == RecurrenceInterval.MONTHLY;
        lastDayBox.setVisible(isMonthly);
        lastDayBox.setManaged(isMonthly);

        if (isMonthly) {
            dateLabel.setText("Day of Month (select any date)");
        } else {
            dateLabel.setText("Day");
            lastDayCheckbox.setSelected(false);
        }

        boolean showDate = !isMonthly || !lastDayCheckbox.isSelected();
        dateBox.setVisible(showDate);
        dateBox.setManaged(showDate);
    }

    private void showEditTemplateDialog(WorkingTemplateDTO template) {
        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        configureDialogOwner(dialog, getOwnerWindow(headerTitle));
        styleDialog(dialog);
        dialog.setTitle("Edit Template");
        dialog.setHeaderText("Edit \"" + template.getRoomName() + "\"");
        dialog.getDialogPane().setPrefWidth(520);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        LocalDate initialBaseDate = template.calculateBaseDate(cleaningScheduleService.getCurrentWeekStart());

        // Frequency selection
        ComboBox<RecurrenceInterval> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(RecurrenceInterval.values());
        freqCombo.setValue(template.getRecurrenceInterval());
        freqCombo.getStyleClass().add("dialog-field");
        freqCombo.setPrefWidth(150);

        Text freqLabel = new Text("Frequency");
        freqLabel.getStyleClass().add("dialog-label-secondary");
        VBox freqBox = new VBox(4, freqLabel, freqCombo);

        // Day picker (date selection)
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select...");
        datePicker.getStyleClass().add("dialog-field");
        datePicker.setValue(initialBaseDate);
        datePicker.setPrefWidth(160);

        Text dateLabel = new Text("Day");
        dateLabel.getStyleClass().add("dialog-label-secondary");
        VBox dateBox = new VBox(4, dateLabel, datePicker);

        // Monthly "last day" option
        Text lastDaySpacer = new Text(" ");
        lastDaySpacer.getStyleClass().add("dialog-label-muted");
        CheckBox lastDayCheckbox = new CheckBox("Last day of month");
        lastDayCheckbox.getStyleClass().add("dialog-label-muted");
        VBox lastDayBox = new VBox(4, lastDaySpacer, lastDayCheckbox);
        lastDayBox.setAlignment(Pos.CENTER_LEFT);
        lastDayBox.setVisible(false);
        lastDayBox.setManaged(false);

        boolean isMonthly = template.getRecurrenceInterval() == RecurrenceInterval.MONTHLY;
        boolean isLastDay = isMonthly && initialBaseDate.getDayOfMonth() == 31;
        lastDayCheckbox.setSelected(isLastDay);

        HBox scheduleRow = new HBox(12, freqBox, dateBox, lastDayBox);
        scheduleRow.setAlignment(Pos.CENTER_LEFT);

        updateScheduleControls(freqCombo.getValue(), dateLabel, dateBox, lastDayCheckbox, lastDayBox);

        // Info about round-robin
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.getStyleClass().add("info-box-success");
        Text infoIcon = new Text("i");
        infoIcon.getStyleClass().add("info-box-success-icon");
        Text infoText = new Text("Assignees rotate automatically - no need to change");
        infoText.getStyleClass().add("info-box-success-text");
        infoBox.getChildren().addAll(infoIcon, infoText);

        content.getChildren().addAll(scheduleRow, infoBox);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDate baseDate = MonthlyScheduleUtil.resolveBaseDate(datePicker.getValue(), freqCombo.getValue(),
                        lastDayCheckbox.isSelected(), initialBaseDate);
                if (baseDate == null) {
                    return null;
                }
                template.updateFromBaseDate(baseDate);
                template.setRecurrenceInterval(freqCombo.getValue());
                hasUnsavedChanges = true;
            }
            return null;
        });

        dialog.getDialogPane().lookupButton(saveButtonType).setDisable(false);
        datePicker.valueProperty().addListener(
                (o, oldV, newV) -> updateSaveButton(dialog, saveButtonType, freqCombo, datePicker, lastDayCheckbox));
        freqCombo.valueProperty().addListener((o, oldV, newV) -> {
            updateScheduleControls(newV, dateLabel, dateBox, lastDayCheckbox, lastDayBox);
            updateSaveButton(dialog, saveButtonType, freqCombo, datePicker, lastDayCheckbox);
        });
        lastDayCheckbox.selectedProperty().addListener((o, oldV, newV) -> {
            updateScheduleControls(freqCombo.getValue(), dateLabel, dateBox, lastDayCheckbox, lastDayBox);
            updateSaveButton(dialog, saveButtonType, freqCombo, datePicker, lastDayCheckbox);
        });

        dialog.showAndWait();
        refreshView();
    }

    @FXML
    public void returnToCleaningSchedule(ActionEvent actionEvent) {
        loadScene(headerTitle.getScene(), "/cleaning/cleaning_schedule.fxml");
    }

    private void deleteTemplate(WorkingTemplateDTO template) {
        boolean confirmed = showConfirmDialog("Delete Template", "Delete \"" + template.getRoomName() + "\"?",
                "This change will be applied when you click 'Save & Apply'.", getOwnerWindow(headerTitle));

        if (confirmed) {
            template.setDeleted(true);
            hasUnsavedChanges = true;
            refreshView();
        }
    }

    @FXML
    public void saveAndApplyTemplate() {
        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null) {
            showErrorAlert("Error", "You must be in a WG.", getOwnerWindow(headerTitle));
            return;
        }

        // First, clear all existing templates and tasks
        cleaningScheduleService.clearTemplates(session.wgId());

        // Then, add all non-deleted templates from working copy
        for (WorkingTemplateDTO wt : workingTemplates) {
            if (wt.isDeleted())
                continue;
            cleaningScheduleService.addTemplateByRoomId(session.wgId(), wt.getRoomId(), DayOfWeek.of(wt.getDayOfWeek()),
                    wt.getRecurrenceInterval(), wt.getBaseWeekStart());
        }

        hasUnsavedChanges = false;

        // Capture scene reference before showing alert
        javafx.scene.Scene currentScene = headerTitle.getScene();

        showSuccessAlert("Template Applied", "The template has been saved and applied to the schedule.",
                getOwnerWindow(headerTitle));

        // Navigate back to cleaning schedule to show the result
        loadScene(currentScene, "/cleaning/cleaning_schedule.fxml");
    }

    @FXML
    public void clearAllTemplates() {
        UserSessionDTO session = sessionManager.getCurrentUserSession().orElse(null);
        if (session == null || session.wgId() == null)
            return;

        boolean confirmed = showConfirmDialog("Clear All", "Clear all template tasks?",
                "This change will be applied when you click 'Save & Apply'.", getOwnerWindow(headerTitle));

        if (confirmed) {
            // Mark all as deleted
            for (WorkingTemplateDTO wt : workingTemplates) {
                wt.setDeleted(true);
            }
            hasUnsavedChanges = true;
            refreshView();
        }
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
        ButtonType discardButton = new ButtonType("Discard Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirm = createStyledConfirmDialog("Unsaved Changes", "You have unsaved changes",
                "Do you want to discard your changes and go back?", getOwnerWindow(headerTitle), discardButton,
                cancelButton);

        return confirm.showAndWait().orElse(cancelButton) == discardButton;
    }

    @FXML
    public void goToCleaningSchedule() {
        backToCleaningSchedule();
    }
}
