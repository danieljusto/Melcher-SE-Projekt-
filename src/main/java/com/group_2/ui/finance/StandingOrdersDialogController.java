package com.group_2.ui.finance;

import com.group_2.model.finance.StandingOrderFrequency;
import com.group_2.service.core.UserService;
import com.group_2.service.finance.StandingOrderService;
import com.group_2.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.group_2.dto.finance.StandingOrderDTO;
import com.group_2.dto.finance.StandingOrderDTO.DebtorShareDTO;
import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.model.finance.StandingOrderFrequency;

@Component
public class StandingOrdersDialogController extends com.group_2.ui.core.Controller {

    private final StandingOrderService standingOrderService;
    private final SessionManager sessionManager;
    private final UserService userService;
    private final DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private StackPane dialogOverlay;
    @FXML
    private TableView<StandingOrderDTO> standingOrdersTable;
    @FXML
    private TableColumn<StandingOrderDTO, String> descriptionColumn;
    @FXML
    private TableColumn<StandingOrderDTO, String> payerColumn;
    @FXML
    private TableColumn<StandingOrderDTO, String> debtorsColumn;
    @FXML
    private TableColumn<StandingOrderDTO, String> amountColumn;
    @FXML
    private TableColumn<StandingOrderDTO, String> frequencyColumn;
    @FXML
    private TableColumn<StandingOrderDTO, String> nextExecutionColumn;
    @FXML
    private TableColumn<StandingOrderDTO, Void> actionsColumn;
    @FXML
    private VBox emptyState;

    private Runnable onOrdersChanged;

    @Autowired
    public StandingOrdersDialogController(StandingOrderService standingOrderService, SessionManager sessionManager,
            UserService userService) {
        this.standingOrderService = standingOrderService;
        this.sessionManager = sessionManager;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {
        // Description column
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description()));

        // Payer column
        payerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().creditorName()));

        // Debtors column
        debtorsColumn.setCellValueFactory(cellData -> {
            String debtorNames = parseDebtorNames(cellData.getValue().debtors());
            return new SimpleStringProperty(debtorNames);
        });

        // Amount column
        amountColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(currencyFormat.format(cellData.getValue().totalAmount())));

        // Frequency column
        frequencyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatFrequency(cellData.getValue())));

        // Next execution column
        nextExecutionColumn.setCellValueFactory(cellData -> {
            String nextDate = "N/A";
            if (cellData.getValue().nextExecution() != null) {
                nextDate = cellData.getValue().nextExecution().format(dateFormatter);
            }
            return new SimpleStringProperty(nextDate);
        });
        actionsColumn.setCellFactory(col -> new TableCell<StandingOrderDTO, Void>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8;");
                editBtn.setOnAction(e -> {
                    StandingOrderDTO order = getTableView().getItems().get(getIndex());
                    showEditDialog(order);
                });

                deleteBtn.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8;");
                deleteBtn.setOnAction(e -> {
                    StandingOrderDTO order = getTableView().getItems().get(getIndex());
                    confirmAndDelete(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StandingOrderDTO order = getTableView().getItems().get(getIndex());
                    User currentUser = sessionManager.getCurrentUser();

                    // Only show actions for creator
                    if (currentUser != null && order.createdById().equals(currentUser.getId())) {
                        HBox buttons = new HBox(5, editBtn, deleteBtn);
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Add double click listener
        standingOrdersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && standingOrdersTable.getSelectionModel().getSelectedItem() != null) {
                StandingOrderDTO selectedOrder = standingOrdersTable.getSelectionModel().getSelectedItem();
                User currentUser = sessionManager.getCurrentUser();

                if (currentUser != null && selectedOrder.createdById().equals(currentUser.getId())) {
                    showEditDialog(selectedOrder);
                }
            }
        });
    }

    private String formatFrequency(StandingOrderDTO order) {
        StandingOrderFrequency freq = order.frequency();
        switch (freq) {
        case WEEKLY:
            return "Weekly";
        case BI_WEEKLY:
            return "Bi-weekly";
        case MONTHLY:
            if (Boolean.TRUE.equals(order.monthlyLastDay())) {
                return "Monthly (last day)";
            } else if (order.monthlyDay() != null) {
                return "Monthly (" + order.monthlyDay() + getDaySuffix(order.monthlyDay()) + ")";
            } else {
                return "Monthly";
            }
        default:
            return freq.toString();
        }
    }

    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
        case 1:
            return "st";
        case 2:
            return "nd";
        case 3:
            return "rd";
        default:
            return "th";
        }
    }

    private String parseDebtorNames(List<DebtorShareDTO> debtors) {
        if (debtors == null || debtors.isEmpty()) {
            return "None";
        } else if (debtors.size() == 1) {
            return debtors.get(0).userName();
        } else {
            return debtors.stream().map(d -> d.userName() + " (" + d.getFormattedPercentage() + ")")
                    .collect(Collectors.joining(", "));
        }
    }

    private void confirmAndDelete(StandingOrderDTO order) {
        Window owner = dialogOverlay.getScene() != null ? dialogOverlay.getScene().getWindow() : null;
        boolean confirmed = showConfirmDialog("Delete Standing Order", "Are you sure?",
                "This will deactivate the standing order: " + order.description(), owner);

        if (confirmed) {
            standingOrderService.deactivateStandingOrder(order.id());
            loadStandingOrders();
            if (onOrdersChanged != null) {
                onOrdersChanged.run();
            }
        }
    }

    public void showDialog() {
        loadStandingOrders();
        dialogOverlay.setVisible(true);
        dialogOverlay.setManaged(true);
    }

    @FXML
    public void closeDialog() {
        dialogOverlay.setVisible(false);
        dialogOverlay.setManaged(false);
    }

    private void loadStandingOrders() {
        WG wg = sessionManager.getCurrentUser().getWg();
        if (wg == null) {
            showEmptyState(true);
            return;
        }

        List<StandingOrderDTO> orders = standingOrderService.getActiveStandingOrdersDTO(wg);

        if (orders.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            standingOrdersTable.setItems(FXCollections.observableArrayList(orders));
        }
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
        standingOrdersTable.setVisible(!show);
        standingOrdersTable.setManaged(!show);
    }

    public void setOnOrdersChanged(Runnable callback) {
        this.onOrdersChanged = callback;
    }

    private void showEditDialog(StandingOrderDTO order) {
        // Create edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Standing Order");
        if (dialogOverlay.getScene() != null) {
            dialog.initOwner(dialogOverlay.getScene().getWindow());
        }

        // Create dialog content
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(500);

        // Defaults for create mode
        String defaultDesc = "";
        double tempAmount = 0.0;
        StandingOrderFrequency defaultFreq = StandingOrderFrequency.MONTHLY;
        Boolean defaultMonthlyLastDay = false;
        Integer defaultMonthlyDay = 1;
        List<DebtorShareDTO> defaultDebtors = new java.util.ArrayList<>();

        if (order != null) {
            defaultDesc = order.description();
            tempAmount = order.totalAmount();
            defaultFreq = order.frequency();
            defaultMonthlyLastDay = order.monthlyLastDay();
            defaultMonthlyDay = order.monthlyDay();
            defaultDebtors = order.debtors();
        }
        final double defaultAmount = tempAmount;

        // Description field
        javafx.scene.text.Text descLabel = new javafx.scene.text.Text("Description");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField descField = new TextField(defaultDesc);
        descField.setStyle(
                "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;");

        // Amount field
        javafx.scene.text.Text amountLabel = new javafx.scene.text.Text("Total Amount (€)");
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField amountField = new TextField(String.format("%.2f", defaultAmount));
        amountField.setStyle(
                "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;");

        // Frequency selection
        javafx.scene.text.Text freqLabel = new javafx.scene.text.Text("Frequency");
        freqLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        ComboBox<String> freqComboBox = new ComboBox<>();
        freqComboBox.getItems().addAll("Weekly", "Bi-weekly", "Monthly");
        freqComboBox.setStyle(
                "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Set current frequency
        if (defaultFreq != null) {
            switch (defaultFreq) {
            case WEEKLY -> freqComboBox.setValue("Weekly");
            case BI_WEEKLY -> freqComboBox.setValue("Bi-weekly");
            case MONTHLY -> freqComboBox.setValue("Monthly");
            }
        } else {
            freqComboBox.setValue("Monthly");
        }

        // Monthly options
        VBox monthlyOptions = new VBox(10);
        CheckBox lastDayCheckbox = new CheckBox("Execute on last day of month");
        lastDayCheckbox.setSelected(Boolean.TRUE.equals(defaultMonthlyLastDay));

        javafx.scene.text.Text dayLabel = new javafx.scene.text.Text("Day of month (1-31):");
        dayLabel.setStyle("-fx-font-size: 12px;");
        TextField dayField = new TextField(defaultMonthlyDay != null ? defaultMonthlyDay.toString() : "1");
        dayField.setPrefWidth(60);
        dayField.setStyle(
                "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");

        HBox dayBox = new HBox(10, dayLabel, dayField);
        dayBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        monthlyOptions.getChildren().addAll(lastDayCheckbox, dayBox);
        monthlyOptions.setVisible("Monthly".equals(freqComboBox.getValue()));
        monthlyOptions.setManaged("Monthly".equals(freqComboBox.getValue()));

        freqComboBox.setOnAction(e -> {
            boolean isMonthly = "Monthly".equals(freqComboBox.getValue());
            monthlyOptions.setVisible(isMonthly);
            monthlyOptions.setManaged(isMonthly);
        });

        lastDayCheckbox.setOnAction(e -> {
            dayBox.setVisible(!lastDayCheckbox.isSelected());
            dayBox.setManaged(!lastDayCheckbox.isSelected());
        });
        dayBox.setVisible(!lastDayCheckbox.isSelected());
        dayBox.setManaged(!lastDayCheckbox.isSelected());

        content.getChildren().addAll(descLabel, descField, amountLabel, amountField, freqLabel, freqComboBox,
                monthlyOptions);

        // Parse existing debtor data
        List<Long> originalDebtorIds = new java.util.ArrayList<>();
        List<Double> originalPercentages = new java.util.ArrayList<>();
        parseDebtorDataForEdit(defaultDebtors, originalDebtorIds, originalPercentages);

        // Split editing - only show if multiple debtors
        java.util.Map<Long, TextField> splitFields = new java.util.HashMap<>();
        final String[] currentMode = { "AMOUNT" };
        VBox splitsContainer = new VBox(8);
        javafx.scene.text.Text validationLabel = new javafx.scene.text.Text("");

        if (originalDebtorIds.size() > 1) {
            javafx.scene.text.Text splitsLabel = new javafx.scene.text.Text("Split Options");
            splitsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            content.getChildren().add(splitsLabel);

            // Mode selector
            HBox modeSelector = new HBox(10);
            modeSelector.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            ToggleGroup modeGroup = new ToggleGroup();
            RadioButton equalBtn = new RadioButton("Equal");
            equalBtn.setToggleGroup(modeGroup);
            RadioButton percentBtn = new RadioButton("Percentage");
            percentBtn.setToggleGroup(modeGroup);
            RadioButton amountBtn = new RadioButton("Custom Amount");
            amountBtn.setToggleGroup(modeGroup);
            amountBtn.setSelected(true);

            modeSelector.getChildren().addAll(equalBtn, percentBtn, amountBtn);
            content.getChildren().add(modeSelector);

            splitsContainer.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10; -fx-background-radius: 8;");
            content.getChildren().add(splitsContainer);

            validationLabel.setStyle("-fx-font-size: 11px; -fx-fill: #6b7280;");
            content.getChildren().add(validationLabel);

            // Build a map of userId -> display name
            Map<Long, String> displayNameMap = userService.getDisplayNames(originalDebtorIds);

            // Function to rebuild split fields
            Runnable rebuildSplitFields = () -> {
                splitsContainer.getChildren().clear();
                splitFields.clear();

                double total;
                final double amountFallback = defaultAmount; // Capture for lambda
                try {
                    total = Double.parseDouble(amountField.getText().replace(",", "."));
                } catch (NumberFormatException ex) {
                    total = amountFallback;
                }

                if (currentMode[0].equals("EQUAL")) {
                    double equalAmount = total / originalDebtorIds.size();
                    for (int i = 0; i < originalDebtorIds.size(); i++) {
                        Long userId = originalDebtorIds.get(i);
                        String name = displayNameMap.getOrDefault(userId, "User " + userId);

                        HBox row = new HBox(10);
                        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        javafx.scene.text.Text nameText = new javafx.scene.text.Text(
                                name + ": " + String.format("%.2f", equalAmount) + "€");
                        nameText.setStyle("-fx-font-size: 12px;");
                        row.getChildren().add(nameText);
                        splitsContainer.getChildren().add(row);
                    }
                    validationLabel.setText("✓ Equal split");
                    validationLabel.setStyle("-fx-font-size: 11px; -fx-fill: #10b981; -fx-font-weight: 600;");
                } else if (currentMode[0].equals("PERCENT")) {
                    for (int i = 0; i < originalDebtorIds.size(); i++) {
                        Long userId = originalDebtorIds.get(i);
                        String name = displayNameMap.getOrDefault(userId, "User " + userId);
                        Double pct = i < originalPercentages.size() ? originalPercentages.get(i)
                                : 100.0 / originalDebtorIds.size();

                        HBox row = new HBox(10);
                        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        javafx.scene.text.Text nameText = new javafx.scene.text.Text(name + ":");
                        nameText.setStyle("-fx-font-size: 12px;");
                        nameText.setWrappingWidth(120);

                        TextField field = new TextField(String.format("%.1f", pct));
                        field.setStyle(
                                "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 4; -fx-padding: 6;");
                        field.setPrefWidth(70);
                        splitFields.put(userId, field);

                        // Live validation listener
                        field.textProperty().addListener((obs, oldVal, newVal) -> {
                            try {
                                double sum = 0;
                                for (TextField f : splitFields.values()) {
                                    sum += Double.parseDouble(f.getText().replace(",", "."));
                                }
                                double remaining = 100.0 - sum;
                                String style;
                                if (Math.abs(remaining) < 0.01) {
                                    style = "-fx-font-size: 11px; -fx-fill: #10b981; -fx-font-weight: 600;";
                                } else if (remaining < 0) {
                                    style = "-fx-font-size: 11px; -fx-fill: #ef4444; -fx-font-weight: 600;";
                                } else {
                                    style = "-fx-font-size: 11px; -fx-fill: #6b7280;";
                                }
                                validationLabel
                                        .setText(String.format("Total: %.1f%% of 100%%\n%.1f%% left", sum, remaining));
                                validationLabel.setStyle(style);
                            } catch (NumberFormatException ex) {
                                validationLabel.setText("⚠ Invalid number");
                                validationLabel.setStyle("-fx-font-size: 11px; -fx-fill: #ef4444;");
                            }
                        });

                        javafx.scene.text.Text percentSign = new javafx.scene.text.Text("%");
                        percentSign.setStyle("-fx-font-size: 12px;");

                        row.getChildren().addAll(nameText, field, percentSign);
                        splitsContainer.getChildren().add(row);
                    }
                    double sum = originalPercentages.stream().mapToDouble(Double::doubleValue).sum();
                    double remaining = 100.0 - sum;
                    String style = Math.abs(remaining) < 0.01
                            ? "-fx-font-size: 11px; -fx-fill: #10b981; -fx-font-weight: 600;"
                            : "-fx-font-size: 11px; -fx-fill: #6b7280;";
                    validationLabel.setText(String.format("Total: %.1f%% of 100%%\n%.1f%% left", sum, remaining));
                    validationLabel.setStyle(style);
                } else { // AMOUNT
                    for (int i = 0; i < originalDebtorIds.size(); i++) {
                        Long userId = originalDebtorIds.get(i);
                        String name = displayNameMap.getOrDefault(userId, "User " + userId);
                        Double pct = i < originalPercentages.size() ? originalPercentages.get(i)
                                : 100.0 / originalDebtorIds.size();
                        double amount = (pct / 100.0) * total;

                        HBox row = new HBox(10);
                        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        javafx.scene.text.Text nameText = new javafx.scene.text.Text(name + ":");
                        nameText.setStyle("-fx-font-size: 12px;");
                        nameText.setWrappingWidth(120);

                        TextField field = new TextField(String.format("%.2f", amount));
                        field.setStyle(
                                "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 4; -fx-padding: 6;");
                        field.setPrefWidth(80);
                        splitFields.put(userId, field);

                        field.textProperty().addListener((obs, oldVal, newVal) -> {
                            try {
                                double totalAmt = Double.parseDouble(amountField.getText().replace(",", "."));
                                double sum = 0;
                                for (TextField f : splitFields.values()) {
                                    sum += Double.parseDouble(f.getText().replace(",", "."));
                                }
                                double remaining = totalAmt - sum;
                                String style;
                                if (Math.abs(remaining) < 0.01) {
                                    style = "-fx-font-size: 11px; -fx-fill: #10b981; -fx-font-weight: 600;";
                                } else if (remaining < 0) {
                                    style = "-fx-font-size: 11px; -fx-fill: #ef4444; -fx-font-weight: 600;";
                                } else {
                                    style = "-fx-font-size: 11px; -fx-fill: #6b7280;";
                                }
                                validationLabel.setText(
                                        String.format("Total: %.2f€ of %.2f€\n%.2f€ left", sum, totalAmt, remaining));
                                validationLabel.setStyle(style);
                            } catch (NumberFormatException ex) {
                                validationLabel.setText("⚠ Invalid number");
                                validationLabel.setStyle("-fx-font-size: 11px; -fx-fill: #ef4444;");
                            }
                        });

                        javafx.scene.text.Text euroSign = new javafx.scene.text.Text("€");
                        euroSign.setStyle("-fx-font-size: 12px;");

                        row.getChildren().addAll(nameText, field, euroSign);
                        splitsContainer.getChildren().add(row);
                    }
                    double sum = 0;
                    for (int i = 0; i < originalDebtorIds.size(); i++) {
                        Double pct = i < originalPercentages.size() ? originalPercentages.get(i)
                                : 100.0 / originalDebtorIds.size();
                        sum += (pct / 100.0) * total;
                    }
                    double remaining = total - sum;
                    String style = Math.abs(remaining) < 0.01
                            ? "-fx-font-size: 11px; -fx-fill: #10b981; -fx-font-weight: 600;"
                            : "-fx-font-size: 11px; -fx-fill: #6b7280;";
                    validationLabel.setText(String.format("Total: %.2f€ of %.2f€\n%.2f€ left", sum, total, remaining));
                    validationLabel.setStyle(style);
                }
            };

            equalBtn.setOnAction(e -> {
                currentMode[0] = "EQUAL";
                rebuildSplitFields.run();
            });
            percentBtn.setOnAction(e -> {
                currentMode[0] = "PERCENT";
                rebuildSplitFields.run();
            });
            amountBtn.setOnAction(e -> {
                currentMode[0] = "AMOUNT";
                rebuildSplitFields.run();
            });

            rebuildSplitFields.run();
        } else {
            // Single debtor info
            String debtorText = defaultDebtors.isEmpty() ? "None" : parseDebtorNames(defaultDebtors);
            javafx.scene.text.Text debtorsLabel = new javafx.scene.text.Text("Debtor: " + debtorText);
            debtorsLabel.setStyle("-fx-font-size: 12px; -fx-fill: #6b7280;");
            content.getChildren().add(debtorsLabel);
        }

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType saveButton = new ButtonType("💾 Save Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Style the save button
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; "
                + "-fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");

        // Handle result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButton) {
            try {
                // Parse updated values
                String newDescription = descField.getText().trim();
                double newAmount = Double.parseDouble(amountField.getText().replace(",", "."));

                StandingOrderFrequency newFrequency = switch (freqComboBox.getValue()) {
                case "Weekly" -> StandingOrderFrequency.WEEKLY;
                case "Bi-weekly" -> StandingOrderFrequency.BI_WEEKLY;
                case "Monthly" -> StandingOrderFrequency.MONTHLY;
                default -> order != null ? order.frequency() : StandingOrderFrequency.MONTHLY;
                };

                Integer newMonthlyDay = null;
                Boolean newMonthlyLastDay = false;

                if (newFrequency == StandingOrderFrequency.MONTHLY) {
                    newMonthlyLastDay = lastDayCheckbox.isSelected();
                    if (!newMonthlyLastDay) {
                        newMonthlyDay = Integer.parseInt(dayField.getText().trim());
                        if (newMonthlyDay < 1 || newMonthlyDay > 31) {
                            throw new IllegalArgumentException("Day must be between 1 and 31");
                        }
                    }
                }

                // Calculate new percentages based on mode
                List<Long> debtorIds = new java.util.ArrayList<>();
                List<Double> percentages = new java.util.ArrayList<>();

                if (originalDebtorIds.size() > 1) {
                    if (currentMode[0].equals("EQUAL")) {
                        double equalPercent = 100.0 / originalDebtorIds.size();
                        for (Long userId : originalDebtorIds) {
                            debtorIds.add(userId);
                            percentages.add(equalPercent);
                        }
                    } else if (currentMode[0].equals("PERCENT")) {
                        double sum = 0;
                        for (Long userId : originalDebtorIds) {
                            TextField field = splitFields.get(userId);
                            double pct = Double.parseDouble(field.getText().replace(",", "."));
                            sum += pct;
                            debtorIds.add(userId);
                            percentages.add(pct);
                        }
                        if (Math.abs(sum - 100.0) > 0.1) {
                            throw new IllegalArgumentException(
                                    String.format("Percentages must sum to 100%% (current: %.1f%%)", sum));
                        }
                    } else { // AMOUNT
                        double totalSplit = 0;
                        for (Long userId : originalDebtorIds) {
                            TextField field = splitFields.get(userId);
                            totalSplit += Double.parseDouble(field.getText().replace(",", "."));
                        }
                        if (Math.abs(totalSplit - newAmount) > 0.01) {
                            throw new IllegalArgumentException(String
                                    .format("Split amounts (€%.2f) must equal total (€%.2f)", totalSplit, newAmount));
                        }
                        for (Long userId : originalDebtorIds) {
                            TextField field = splitFields.get(userId);
                            double amount = Double.parseDouble(field.getText().replace(",", "."));
                            debtorIds.add(userId);
                            percentages.add((amount / newAmount) * 100.0);
                        }
                    }
                } else {
                    debtorIds.addAll(originalDebtorIds);
                    percentages.addAll(originalPercentages);
                }

                // Create or Update
                if (order == null) {
                    // Create mode
                    standingOrderService.createStandingOrderDTO(sessionManager.getCurrentUser().getId(), // Creator
                            sessionManager.getCurrentUser().getId(), // Creditor (self)
                            sessionManager.getCurrentUser().getWg().getId(), // WG
                            newAmount, newDescription, newFrequency, null, // Start date (defaults to now/next execution
                                                                           // in service?) Service expect LocalDate
                                                                           // startDate
                            debtorIds, percentages.isEmpty() ? null : percentages, newMonthlyDay, newMonthlyLastDay);
                } else {
                    // Update mode
                    standingOrderService.updateStandingOrderDTO(order.id(), sessionManager.getCurrentUser().getId(),
                            order.creditorId(), newAmount, newDescription, newFrequency, debtorIds,
                            percentages.isEmpty() ? null : percentages, newMonthlyDay, newMonthlyLastDay);
                }

                // Refresh the table
                loadStandingOrders();

                if (onOrdersChanged != null) {
                    onOrdersChanged.run();
                }

                // Show success message
                Window owner = dialogOverlay.getScene() != null ? dialogOverlay.getScene().getWindow() : null;
                showSuccessAlert("Success", "Standing order updated successfully.", owner);

            } catch (NumberFormatException e) {
                Window owner = dialogOverlay.getScene() != null ? dialogOverlay.getScene().getWindow() : null;
                showErrorAlert("Invalid input", "Please enter valid numbers.", owner);
            } catch (Exception e) {
                Window owner = dialogOverlay.getScene() != null ? dialogOverlay.getScene().getWindow() : null;
                showErrorAlert("Failed to update standing order", e.getMessage(), owner);
            }
        }
    }

    private void parseDebtorDataForEdit(List<DebtorShareDTO> debtors, List<Long> debtorIds, List<Double> percentages) {
        if (debtors == null)
            return;

        for (DebtorShareDTO d : debtors) {
            debtorIds.add(d.userId());
            percentages.add(d.percentage());
        }
    }
}
