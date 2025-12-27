package com.group_2.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group_2.repository.UserRepository;
import com.group_2.service.StandingOrderService;
import com.group_2.util.SessionManager;
import com.model.StandingOrder;
import com.model.StandingOrderFrequency;
import com.model.User;
import com.model.WG;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StandingOrdersDialogController {

    private final StandingOrderService standingOrderService;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private StackPane dialogOverlay;
    @FXML
    private TableView<StandingOrder> standingOrdersTable;
    @FXML
    private TableColumn<StandingOrder, String> descriptionColumn;
    @FXML
    private TableColumn<StandingOrder, String> payerColumn;
    @FXML
    private TableColumn<StandingOrder, String> debtorsColumn;
    @FXML
    private TableColumn<StandingOrder, String> amountColumn;
    @FXML
    private TableColumn<StandingOrder, String> frequencyColumn;
    @FXML
    private TableColumn<StandingOrder, String> nextExecutionColumn;
    @FXML
    private TableColumn<StandingOrder, Void> actionsColumn;
    @FXML
    private VBox emptyState;

    private Runnable onOrdersChanged;

    @Autowired
    public StandingOrdersDialogController(StandingOrderService standingOrderService,
            SessionManager sessionManager, UserRepository userRepository) {
        this.standingOrderService = standingOrderService;
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
    }

    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {
        // Description column
        descriptionColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        // Payer column
        payerColumn.setCellValueFactory(cellData -> {
            User creditor = cellData.getValue().getCreditor();
            String name = creditor.getName();
            if (creditor.getSurname() != null && !creditor.getSurname().isEmpty()) {
                name += " " + creditor.getSurname().charAt(0) + ".";
            }
            return new SimpleStringProperty(name);
        });

        // Debtors column
        debtorsColumn.setCellValueFactory(cellData -> {
            String debtorNames = parseDebtorNames(cellData.getValue().getDebtorData());
            return new SimpleStringProperty(debtorNames);
        });

        // Amount column
        amountColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotalAmount())));

        // Frequency column
        frequencyColumn.setCellValueFactory(cellData -> {
            StandingOrder order = cellData.getValue();
            String freqText = formatFrequency(order);
            return new SimpleStringProperty(freqText);
        });

        // Next execution column
        nextExecutionColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getNextExecution().format(dateFormatter)));

        // Actions column with delete button
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑️ Delete");

            {
                deleteBtn.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                deleteBtn.setOnAction(e -> {
                    StandingOrder order = getTableView().getItems().get(getIndex());
                    confirmAndDeleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }

    private String formatFrequency(StandingOrder order) {
        StandingOrderFrequency freq = order.getFrequency();
        switch (freq) {
            case WEEKLY:
                return "Weekly";
            case BI_WEEKLY:
                return "Bi-weekly";
            case MONTHLY:
                if (Boolean.TRUE.equals(order.getMonthlyLastDay())) {
                    return "Monthly (last day)";
                } else if (order.getMonthlyDay() != null) {
                    return "Monthly (" + order.getMonthlyDay() + getDaySuffix(order.getMonthlyDay()) + ")";
                } else {
                    return "Monthly";
                }
            default:
                return freq.toString();
        }
    }

    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13)
            return "th";
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

    private String parseDebtorNames(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        try {
            List<Map<String, Object>> debtorList = objectMapper.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            List<String> names = debtorList.stream()
                    .map(entry -> {
                        Object userIdObj = entry.get("userId");
                        Long userId = userIdObj instanceof Number ? ((Number) userIdObj).longValue()
                                : Long.parseLong(userIdObj.toString());
                        return userRepository.findById(userId)
                                .map(user -> {
                                    String name = user.getName();
                                    if (user.getSurname() != null && !user.getSurname().isEmpty()) {
                                        name += " " + user.getSurname().charAt(0) + ".";
                                    }
                                    return name;
                                })
                                .orElse("Unknown");
                    })
                    .collect(Collectors.toList());

            return String.join(", ", names);
        } catch (Exception e) {
            return "Error";
        }
    }

    private void confirmAndDeleteOrder(StandingOrder order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        if (dialogOverlay.getScene() != null) {
            confirm.initOwner(dialogOverlay.getScene().getWindow());
        }
        confirm.setTitle("Delete Standing Order");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will deactivate the standing order: " + order.getDescription());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            standingOrderService.deactivateStandingOrder(order.getId());
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

        List<StandingOrder> orders = standingOrderService.getActiveStandingOrders(wg);

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
}
