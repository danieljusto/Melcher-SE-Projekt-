package com.group_2.ui;

import com.group_2.service.TransactionService;
import com.group_2.util.SessionManager;
import com.model.User;
import com.model.WG;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component
public class TransactionsController extends Controller {

    private final TransactionService transactionService;
    private final SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    // Balance display
    @FXML
    private Text totalBalanceText;

    // Balance table
    @FXML
    private TableView<BalanceEntry> balanceTable;
    @FXML
    private TableColumn<BalanceEntry, String> memberColumn;
    @FXML
    private TableColumn<BalanceEntry, String> balanceColumn;

    // Navbar
    @FXML
    private NavbarController navbarController;

    // Balance card
    @FXML
    private VBox balanceCard;

    private DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");

    @Autowired
    public TransactionsController(TransactionService transactionService,
            SessionManager sessionManager) {
        this.transactionService = transactionService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        if (navbarController != null) {
            navbarController.setTitle("💰 Transactions");
        }
        setupBalanceTable();
    }

    public void initView() {
        sessionManager.refreshCurrentUser();
        updateBalanceDisplay();
        updateBalanceSheet();
    }

    private void setupBalanceTable() {
        memberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemberName()));

        balanceColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBalanceFormatted()));

        balanceColumn.setCellFactory(column -> new TableCell<BalanceEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    BalanceEntry entry = getTableView().getItems().get(getIndex());
                    double balance = entry.getBalance();

                    if (balance > 0) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if (balance < 0) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b; -fx-font-weight: normal;");
                    }
                }
            }
        });

        // Remove placeholder rows and configure table properly
        balanceTable.setPlaceholder(new Text("No balance data available"));
        balanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        balanceTable.setFocusTraversable(false);

        // Make rows clickable for settlement
        balanceTable.setRowFactory(tv -> {
            TableRow<BalanceEntry> row = new TableRow<>();
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    BalanceEntry entry = row.getItem();
                    if (entry != null && entry.getBalance() != 0) {
                        showSettlementDialog(entry);
                    }
                }
            });
            return row;
        });

        // Add a listener to dynamically resize the table based on the number of items
        balanceTable.getItems()
                .addListener((javafx.collections.ListChangeListener.Change<? extends BalanceEntry> c) -> {
                    updateBalanceTableHeight();
                });
    }

    private void updateBalanceTableHeight() {
        int rowCount = balanceTable.getItems().size();
        if (rowCount == 0) {
            balanceTable.setPrefHeight(100);
            balanceTable.setMaxHeight(100);
            balanceTable.setMinHeight(100);
        } else {
            // Calculate exact height: header (40px) + rows (50px each)
            double height = 40 + (rowCount * 50);
            balanceTable.setPrefHeight(height);
            balanceTable.setMaxHeight(height);
            balanceTable.setMinHeight(height);
        }
    }

    private void updateBalanceDisplay() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        double totalBalance = transactionService.getTotalBalance(currentUser.getId());
        totalBalanceText.setText(currencyFormat.format(totalBalance));

        // Change card color based on balance
        if (totalBalance > 0) {
            // Green gradient - they owe you
            balanceCard.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #10b981, #059669); -fx-padding: 25;");
        } else if (totalBalance < 0) {
            // Red gradient - you owe them
            balanceCard.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ef4444, #dc2626); -fx-padding: 25;");
        } else {
            // Blue gradient - all settled
            balanceCard.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #3b82f6, #2563eb); -fx-padding: 25;");
        }
    }

    private void updateBalanceSheet() {
        User currentUser = sessionManager.getCurrentUser();

        balanceTable.getItems().clear();

        if (currentUser == null) {
            return;
        }

        WG wg = currentUser.getWg();

        if (wg == null || wg.mitbewohner == null) {
            return;
        }

        Map<Long, Double> balances = transactionService.calculateAllBalances(currentUser.getId());

        for (User member : wg.mitbewohner) {
            if (!member.getId().equals(currentUser.getId())) {
                double balance = balances.getOrDefault(member.getId(), 0.0);
                String memberName = member.getName() +
                        (member.getSurname() != null ? " " + member.getSurname() : "");
                balanceTable.getItems().add(new BalanceEntry(memberName, balance, member));
            }
        }

        // The listener will automatically update the height
        // But call it explicitly to ensure it happens immediately
        updateBalanceTableHeight();
    }

    private void showSettlementDialog(BalanceEntry entry) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        double balance = entry.getBalance();
        double absBalance = Math.abs(balance);
        String memberName = entry.getMemberName();
        User otherUser = entry.getUser();

        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Settle Balance");

        // Set owner window
        Window owner = balanceTable.getScene().getWindow();
        dialog.initOwner(owner);

        // Create dialog content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(400);

        // Header icon
        Text headerIcon = new Text(balance < 0 ? "💸" : "💰");
        headerIcon.setStyle("-fx-font-size: 48px;");

        // Message
        Text messageText;
        if (balance < 0) {
            // You owe them
            messageText = new Text("You owe " + memberName);
        } else {
            // They owe you
            messageText = new Text(memberName + " owes you");
        }
        messageText.setStyle("-fx-font-size: 16px; -fx-fill: #374151;");

        // Amount
        Text amountText = new Text(currencyFormat.format(absBalance));
        amountText.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: " +
                (balance < 0 ? "#ef4444" : "#10b981") + ";");

        // Payment method selection
        Text paymentLabel = new Text("Select payment method:");
        paymentLabel.setStyle("-fx-font-size: 14px; -fx-fill: #6b7280;");

        ToggleGroup paymentGroup = new ToggleGroup();

        // Cash option
        RadioButton cashOption = new RadioButton("Cash");
        cashOption.setToggleGroup(paymentGroup);
        cashOption.setSelected(true);
        cashOption.setStyle("-fx-font-size: 14px;");
        HBox cashBox = new HBox(10, new Text("💵"), cashOption);
        cashBox.setAlignment(Pos.CENTER_LEFT);

        // Bank transfer option
        RadioButton bankOption = new RadioButton("Bank Transfer");
        bankOption.setToggleGroup(paymentGroup);
        bankOption.setStyle("-fx-font-size: 14px;");
        HBox bankBox = new HBox(10, new Text("🏦"), bankOption);
        bankBox.setAlignment(Pos.CENTER_LEFT);

        // PayPal option
        RadioButton paypalOption = new RadioButton("PayPal");
        paypalOption.setToggleGroup(paymentGroup);
        paypalOption.setStyle("-fx-font-size: 14px;");

        // Load PayPal icon
        ImageView paypalIcon;
        try {
            Image paypalImage = new Image(getClass().getResourceAsStream("/icon_paypal.png"));
            paypalIcon = new ImageView(paypalImage);
            paypalIcon.setFitWidth(20);
            paypalIcon.setFitHeight(20);
            paypalIcon.setPreserveRatio(true);
        } catch (Exception e) {
            paypalIcon = new ImageView();
        }
        HBox paypalBox = new HBox(10, paypalIcon, paypalOption);
        paypalBox.setAlignment(Pos.CENTER_LEFT);

        VBox paymentMethods = new VBox(12, cashBox, bankBox, paypalBox);
        paymentMethods.setAlignment(Pos.CENTER_LEFT);
        paymentMethods.setPadding(new Insets(10, 0, 10, 40));
        paymentMethods.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 15;");

        content.getChildren().addAll(headerIcon, messageText, amountText, paymentLabel, paymentMethods);

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType settleButton = new ButtonType("✓ Confirm Settlement", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(settleButton, cancelButton);

        // Style the settle button
        Button settleBtn = (Button) dialog.getDialogPane().lookupButton(settleButton);
        settleBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; " +
                "-fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");

        // Handle result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == settleButton) {
            // Get selected payment method
            String paymentMethod = "Cash";
            if (bankOption.isSelected()) {
                paymentMethod = "Bank Transfer";
            } else if (paypalOption.isSelected()) {
                paymentMethod = "PayPal";
            }

            // Create settlement transaction
            createSettlementTransaction(currentUser, otherUser, absBalance, paymentMethod, balance < 0);
        }
    }

    private void createSettlementTransaction(User currentUser, User otherUser, double amount,
            String paymentMethod, boolean currentUserPays) {
        try {
            // Determine payer and debtor
            Long payerId;
            Long debtorId;
            String description;

            if (currentUserPays) {
                // Current user is paying off their debt
                payerId = currentUser.getId();
                debtorId = otherUser.getId();
                description = "Settlement via " + paymentMethod + " (paid to " +
                        otherUser.getName() + (otherUser.getSurname() != null ? " " + otherUser.getSurname() : "")
                        + ")";
            } else {
                // Other user is paying off their debt to current user
                payerId = otherUser.getId();
                debtorId = currentUser.getId();
                description = "Settlement via " + paymentMethod + " (received from " +
                        otherUser.getName() + (otherUser.getSurname() != null ? " " + otherUser.getSurname() : "")
                        + ")";
            }

            // Create the transaction
            transactionService.createTransaction(
                    payerId,
                    List.of(debtorId),
                    null, // Equal split (100% to single debtor)
                    amount,
                    description);

            // Refresh the display
            updateBalanceDisplay();
            updateBalanceSheet();

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Settlement Complete",
                    "The balance with " + otherUser.getName() +
                            (otherUser.getSurname() != null ? " " + otherUser.getSurname() : "") +
                            " has been settled.",
                    balanceTable.getScene().getWindow());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Settlement Failed",
                    "Could not create settlement: " + e.getMessage(),
                    balanceTable.getScene().getWindow());
        }
    }

    @FXML
    public void showAddTransactionDialog() {
        try {
            TransactionDialogController dialogController = applicationContext
                    .getBean(TransactionDialogController.class);

            // Set callback to refresh when transaction is saved
            dialogController.setOnTransactionSaved(() -> {
                updateBalanceDisplay();
                updateBalanceSheet();
            });

            dialogController.showDialog();
        } catch (Exception e) {
            System.err.println("Error showing transaction dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToHistory() {
        loadScene(balanceTable.getScene(), "/transaction_history.fxml");
        javafx.application.Platform.runLater(() -> {
            TransactionHistoryController historyController = applicationContext
                    .getBean(TransactionHistoryController.class);
            historyController.initView();
        });
    }

    @FXML
    public void showStandingOrders() {
        try {
            StandingOrdersDialogController dialogController = applicationContext
                    .getBean(StandingOrdersDialogController.class);
            dialogController.showDialog();
        } catch (Exception e) {
            System.err.println("Error showing standing orders dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class BalanceEntry {
        private final String memberName;
        private final double balance;
        private final User user;
        private final DecimalFormat format = new DecimalFormat("€#,##0.00");

        public BalanceEntry(String memberName, double balance, User user) {
            this.memberName = memberName;
            this.balance = balance;
            this.user = user;
        }

        public String getMemberName() {
            return memberName;
        }

        public double getBalance() {
            return balance;
        }

        public User getUser() {
            return user;
        }

        public String getBalanceFormatted() {
            return format.format(balance);
        }
    }
}
